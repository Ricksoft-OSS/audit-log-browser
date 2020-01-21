/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.file;

import jp.ricksoft.auditlogbrowser.audit.AuditLogManager;
import jp.ricksoft.auditlogbrowser.util.DateTimeUtil;
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
    private static final Logger LOG = LoggerFactory.getLogger(CSVManager.class);

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
    public File prepareCSV(String name) {

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
    public void addRecord(File csv, Map<String, Object> recordMap)
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

    /**
     * Acquire audit log and create csv file.
     *
     * @param fromDate Start date of the audit log acquisition target period
     * @param fromTime Start time of the audit log acquisition target period
     * @param toDate   End date of audit log acquisition period
     * @param toTime   End time of audit log acquisition period
     * @param user     Username
     */
    @Async
    public void createAuditLogsCSV(String fromDate, String fromTime, String toDate, String toTime, String user) {
        LOG.info("Starting Create Audit log CSV.");

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Long startEpochMilli;
        if (StringUtils.isBlank(fromDate)) {
            startEpochMilli = DateTimeUtil.convertEpochMilli(auditLogManager.getOldestLoggedDateTime());
        } else {
            startEpochMilli = DateTimeUtil.convertFromEpochMilli(fromDate, fromTime);
        }

        Long endEpochMilli = DateTimeUtil.convertToEpochMilli(toDate, toTime);
        LocalDateTime targetDate = DateTimeUtil.convertLocalDateTime(startEpochMilli);

        // Get Daily AuditLogs
        while (targetDate.isBefore(DateTimeUtil.convertLocalDateTime(endEpochMilli)))
        {
            String targetDateStr = targetDate.format(dateFormat.withResolverStyle(ResolverStyle.STRICT));
            Long fromEpochMilli = DateTimeUtil.convertEpochMilli(targetDate);
            Long toEpochMilli = DateTimeUtil.convertEpochMilli(targetDate.toLocalDate().plusDays(1).atStartOfDay()) - 1;
            if (toEpochMilli >= endEpochMilli)
            {
                toEpochMilli = endEpochMilli;
            }

            this.createOneDayAuditLogCSV(targetDateStr, fromEpochMilli, toEpochMilli, user);

            targetDate = targetDate.toLocalDate().plusDays(1).atStartOfDay();

        }

        LOG.info("Finish Create Audit log CSV.");
    }

    public File createOneDayAuditLogCSV(String dateStr, Long fromEpochMilli, Long toEpochMilli, String user) {
        File csv = this.prepareCSV(String.format(csvName, dateStr));
        Long entryId = null;
        List<Map<String, Object>> auditLogs;

        LOG.info("Start Create {} Audit log CSV.", dateStr);
        do {
            auditLogs = auditLogManager.getAuditLogs(fromEpochMilli, toEpochMilli, entryId, user);

            if (auditLogs.isEmpty()) {
                break;
            }

            auditLogs.forEach(entry -> this.addRecord(csv, entry));

            entryId = (Long) auditLogs.get(auditLogs.size() - 1).get(KEY_ID) + 1;

        } while (auditLogs.size() == 100);

        LOG.info("End Create {} Audit log CSV.", dateStr);

        return csv;
    }

    private String convertToStr(Object obj) {
        return (obj == null) ? "" : String.valueOf(obj);
    }
}
