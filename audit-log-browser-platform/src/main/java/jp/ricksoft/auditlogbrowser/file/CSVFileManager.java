/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.file;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVStrategy;

import jp.ricksoft.auditlogbrowser.audit.AuditLogManager;
import jp.ricksoft.auditlogbrowser.util.DateTimeUtil;

public class CSVFileManager
{

    private static final char CSV_DELIMITER = ',';
    private static final char CSV_ENCAPSULATOR = '"';
    private static final char CSV_COMMENT_START = '#';

    private static final String KEY_ID = "id";
    private static final String DELIMITER = ",";
    private static final String LABELS_MESSAGE_ID_DEFAULT = "ricksoft.auditLogBrowser.csv.labels";
    private static final String KEYS_DEFAULT = "AuditLogBrowser.csv.column.default.keys";

    private Properties properties;
    private AuditLogManager auditLogManager;
    private MessageService messageService;

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public void setAuditLogManager(AuditLogManager auditLogManager)
    {
        this.auditLogManager = auditLogManager;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.messageService = serviceRegistry.getMessageService();
    }

    /**
     * Prepare csv file.
     * 
     * @param path  Parent folder path
     * @param name  csv file name
     * @return  csv file
     */
    public File prepareCSV(String path, String name)
    {

        Path csvPath = Paths.get(path, name);
        if(Files.exists(csvPath)) {
            return csvPath.toFile();
        }
        
        try
        {
            Files.createFile(csvPath);
        } catch (IOException e1)
        {
            e1.printStackTrace();
            return null;
        }

        String labelsString = messageService.getMessage(LABELS_MESSAGE_ID_DEFAULT);

        try (FileWriter csvWriter = new FileWriter(csvPath.toFile(), true);)
        {
            
            // TODO: a
            CSVPrinter printer = new CSVPrinter(csvWriter,
                    new CSVStrategy(CSV_DELIMITER, CSV_ENCAPSULATOR, CSV_COMMENT_START));
            printer.println(labelsString.split(DELIMITER));
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
     * @param tempDir temporary directory for csv
     * @param recordMap audit log entry
     */
    public void addRecord(File csv, Map<String, Object> recordMap)
    {

        String[] keys = properties.getProperty(KEYS_DEFAULT).split(DELIMITER);

        try (FileWriter csvWriter = new FileWriter(csv, true);)
        {

            CSVPrinter printer = new CSVPrinter(csvWriter,
                    new CSVStrategy(CSV_DELIMITER, CSV_ENCAPSULATOR, CSV_COMMENT_START));

            List<String> ret = new ArrayList<String>();
            Arrays.stream(keys).forEach(key ->
            {
                if (recordMap.get(key) == null)
                {
                    ret.add("");
                } else
                {
                    ret.add(String.valueOf(recordMap.get(key)));
                }
            });
            String[] record = ret.toArray(new String[0]);
            printer.println(record);
            printer.flush();

        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Check whether csv has row.
     * 
     * @param csv
     * @return
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
     * @param fromDate  Start date of the audit log acquisition target period
     * @param fromTime  Start time of the audit log acquisition target period
     * @param toDate  End date of audit log acquisition period
     * @param toTime  End time of audit log acquisition period
     * @param user  Username
     * @param directory  Directory storing the csv file
     * @throws IOException
     */
    public void createAuditLogsCSV(String fromDate, String fromTime, String toDate, String toTime, String user,
            File directory) throws IOException
    {

        int unitMaxItems = Integer.parseInt(properties.getProperty("AuditLogBrowser.schedule.download.unit-maxsize"));
        String appName = properties.getProperty("AuditLogBrowser.schedule.download.appname");
        String csvName = properties.getProperty("AuditLogBrowser.schedule.download.filename.csv");

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Long startEpochMilli = DateTimeUtil.generateFromEpochMilli(fromDate, fromTime);
        Long endEpochMilli = DateTimeUtil.generateToEpochMilli(toDate, toTime);
        LocalDateTime targetDate = DateTimeUtil.generateLocalDateTime(startEpochMilli);

        // Get Daily AuditLogs
        while (targetDate.isBefore(DateTimeUtil.generateLocalDateTime(endEpochMilli)))
        {
            String targetDateStr = targetDate.format(dateFormat.withResolverStyle(ResolverStyle.STRICT));
            Long fromEpochMilli = DateTimeUtil.convertEpochMilli(targetDate);
            Long toEpochMilli = DateTimeUtil.convertEpochMilli(targetDate.toLocalDate().plusDays(1).atStartOfDay()) - 1;
            if (toEpochMilli >= endEpochMilli)
            {
                toEpochMilli = endEpochMilli;
            }

            Long entryId = null;
            List<Map<String, Object>> auditLogs;

            do
            {
                auditLogs = auditLogManager.getAuditLogs(appName, unitMaxItems, fromEpochMilli, toEpochMilli, entryId,
                        user);

                if (auditLogs.isEmpty())
                {
                    break;
                }

                final File csv = this.prepareCSV(directory.getAbsolutePath(), String.format(csvName, targetDateStr));

                auditLogs.stream().forEach(entry -> this.addRecord(csv, entry));

                // For Next Audit Query Param
                entryId = (Long) auditLogs.get(auditLogs.size() - 1).get(KEY_ID) + 1;

            } while (auditLogs.size() == unitMaxItems);

            targetDate = targetDate.toLocalDate().plusDays(1).atStartOfDay();

        }
    }

}
