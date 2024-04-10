package jp.ricksoft.auditlogbrowser.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

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

public class CSVManager {
    // private static final char CSV_DELIMITER = ',';
    // private static final char CSV_ENCAPSULATOR = '"';
    // private static final char CSV_COMMENT_START = '#';

    private static final String KEY_ID = "id";

    // private static final Logger LOG = LoggerFactory.getLogger(CSVManager.class);

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

    public void setAuditLogManager(AuditLogManager auditLogManager) {
        this.auditLogManager = auditLogManager;
    }

    /**
     * Prepare csv file.
     *
     * @param name csv file name
     * @return csv file
     */
    private File prepareCSV(String name, Path workDirPath) {

        final Path csvPath = workDirPath.resolve(name);
        if (Files.exists(csvPath)) {
            return csvPath.toFile();
        }

        try {
            Files.createFile(csvPath);
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

        try (FileWriter csvWriter = new FileWriter(csvPath.toFile(), true);
                CSVPrinter printer = new CSVPrinter(csvWriter, CSVFormat.EXCEL)) {

            printer.printRecord(Arrays.asList(labels));
            printer.flush();
            return csvPath.toFile();

        } catch (Exception ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Add one csv record.
     * 
     * @param csv       target csv
     * @param recordMap audit log entry
     */
    private void addRecord(File csv, Map<String, Object> recordMap) {

        try (FileWriter csvWriter = new FileWriter(csv, StandardCharsets.UTF_8, true);
                CSVPrinter printer = new CSVPrinter(csvWriter, CSVFormat.EXCEL)) {

            List<String> records = Arrays.stream(keys)
                    .map(key -> convertToStr(recordMap.get(key)))
                    .collect(Collectors.toList());
            printer.printRecord(records);
            printer.flush();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Check whether csv has row.
     * 
     * @param csv target CSV File
     * @return true if record exists
     */
    public boolean hasRecord(File csv) {
        try (Stream<String> st = Files.lines(csv.toPath(), StandardCharsets.UTF_8)) {
            return st.count() > 1;

        } catch (Exception ioe) {
            ioe.printStackTrace();
            return false;
        }
    }

    public File createOneDayAuditLogCSV(long fromEpochMilli, long toEpochMilli, String user,
            Map<String, Serializable> searchValues) {
        return this.createOneDayAuditLogCSV(fromEpochMilli, toEpochMilli, user, searchValues, Paths.get(tmpDirPath));
    }

    public File createOneDayAuditLogCSV(long fromEpochMilli, long toEpochMilli, String user,
            Map<String, Serializable> searchValues, Path workDirPath) {
        final File csv = this.prepareCSV(
                String.format(csvName, DateTimeUtil.convertEpochMilliToYYYYMMDD(fromEpochMilli)), workDirPath);
        if (csv == null) {
            return null;
        }

        Long entryId = null;
        List<Map<String, Object>> auditLogs;

        do {
            auditLogs = auditLogManager.getAuditLogs(fromEpochMilli, toEpochMilli, entryId,
                    user, searchValues);

            if (auditLogs.isEmpty()) {
                return null;
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
