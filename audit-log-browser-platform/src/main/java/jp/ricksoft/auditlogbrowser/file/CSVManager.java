package jp.ricksoft.auditlogbrowser.file;

/*-
 * #%L
 * Audit Log Browser Platform JAR Module
 * %%
 * Copyright (C) 2018 - 2020 Ricksoft Co., Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import jp.ricksoft.auditlogbrowser.audit.AuditLogManager;
import jp.ricksoft.auditlogbrowser.util.DateTimeUtil;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVStrategy;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
public class CSVManager
{
    private static final char CSV_DELIMITER = ',';
    private static final char CSV_ENCAPSULATOR = '"';
    private static final char CSV_COMMENT_START = '#';

    private static final String KEY_ID = "id";

    private String tmpDirPath;
    private String csvName;
    private String[] labels;
    private String[] keys;

    private AuditLogManager auditLogManager;

    public void setTmpDirPath(String tmpDirPath) {
        this.tmpDirPath = tmpDirPath;
    }

    public void setCsvName(String csvName) {
        this.csvName = csvName;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public void setAuditLogManager(AuditLogManager auditLogManager)
    {
        this.auditLogManager = auditLogManager;
    }

    /**
     * Prepare csv file.
     *
     * @param name csv file name
     * @return csv file
     */
    private File prepareCSV(String name) {

        Path csvPath = Paths.get(tmpDirPath, name);
        if (Files.exists(csvPath)) {
            return csvPath.toFile();
        }

        try {
            Files.createFile(csvPath);
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

        try (FileWriter csvWriter = new FileWriter(csvPath.toFile(), true)) {
            CSVPrinter printer = new CSVPrinter(csvWriter,
                    new CSVStrategy(CSV_DELIMITER, CSV_ENCAPSULATOR, CSV_COMMENT_START));
            printer.println(labels);
            printer.flush();
            return csvPath.toFile();

        } catch (Exception ioe)
        {
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Add one csv record.
     * 
     * @param csv target csv
     * @param recordMap audit log entry
     */
    private void addRecord(File csv, Map<String, Object> recordMap)
    {

        try (FileWriter csvWriter = new FileWriter(csv, true))
        {

            CSVPrinter printer = new CSVPrinter(csvWriter,
                    new CSVStrategy(CSV_DELIMITER, CSV_ENCAPSULATOR, CSV_COMMENT_START));

            String[] record = Arrays.stream(keys)
                                    .map(key -> convertToStr(recordMap.get(key)))
                                    .toArray(String[]::new);
            printer.println(record);
            printer.flush();

        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Check whether csv has row.
     * 
     * @param csv target CSV File
     * @return true if record exists
     */
    public boolean hasRecord(File csv)
    {
        try  (Stream<String> st = Files.lines(csv.toPath(), StandardCharsets.UTF_8)){
            return st.count() > 1;
            
        } catch (Exception ioe)
        {
            ioe.printStackTrace();
            return false;
        }
    }

    public File createOneDayAuditLogCSV(long fromEpochMilli, long toEpochMilli, String user) {
        File csv = this.prepareCSV(String.format(csvName, DateTimeUtil.convertEpochMilliToYYYYMMDD(fromEpochMilli)));
        Long entryId = null;
        List<Map<String, Object>> auditLogs;

        do {
            auditLogs = auditLogManager.getAuditLogs(fromEpochMilli, toEpochMilli, entryId, user);

            if (auditLogs.isEmpty()) {
                break;
            }

            auditLogs.forEach(entry -> this.addRecord(csv, entry));

            entryId = (long) auditLogs.get(auditLogs.size() - 1).get(KEY_ID) + 1;

        } while (auditLogs.size() == 100);

        return csv;
    }

    private String convertToStr(Object obj) {
        return (obj == null) ? "" : String.valueOf(obj);
    }
}
