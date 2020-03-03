/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.file;

import jp.ricksoft.auditlogbrowser.audit.AuditLogManager;
import jp.ricksoft.auditlogbrowser.util.DateTimeUtil;
import net.sf.cglib.core.Local;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
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
