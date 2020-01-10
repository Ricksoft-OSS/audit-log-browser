/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.audit_log_browser.schedule;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.ricksoft.audit_log_browser.NodeRef.RepositoryFolderManager;
import jp.ricksoft.audit_log_browser.audit.AuditLogManager;
import jp.ricksoft.audit_log_browser.file.CSVFileManager;
import jp.ricksoft.audit_log_browser.file.FileManager;
import jp.ricksoft.audit_log_browser.file.ZipFileManager;
import jp.ricksoft.audit_log_browser.util.DateUtil;

public class AuditlogArchiveScheduler {

    private static final String KEY_ID = "id";
    private static final String NAME_DAILYZIP = "Auditlogs_%s.zip";
    private static final String MSG_NO_BACKUP_DIRECTORY = "No backup directory set.";
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Logger LOG = LoggerFactory.getLogger(AuditlogArchiveScheduler.class);
    
    private Properties properties;
    private AuditLogManager auditLogManager;
    private CSVFileManager csvManager;
    private ZipFileManager zipManager;
    private RepositoryFolderManager repositoryFolderManager;
    private FileManager fileManager;
    
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    public void setAuditLogManager(AuditLogManager auditLogManager) {
        this.auditLogManager = auditLogManager;
    }
    
    public void setCsvManager(CSVFileManager csvManager) {
        this.csvManager = csvManager;
    }
    
    public void setZipManager(ZipFileManager zipManager) {
        this.zipManager = zipManager;
    }
    
    public void setRepositoryFolderManager(RepositoryFolderManager repositoryFolderManager) {
        this.repositoryFolderManager = repositoryFolderManager;
    }
    
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * Executer implementation
     */
    public void execute(int retentionPeriod, boolean doDelete, String dstFolderPath) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("============ Start Schedule Job.");
        }
        String sysDefaultPath = System.getProperty("java.io.tmpdir");
        String appName = properties.getProperty("AuditLogBrowser.schedule.download.appname");
        String dirName = properties.getProperty("AuditLogBrowser.schedule.download.directoryname.tmp");
        String csvNameFormat = properties.getProperty("AuditLogBrowser.schedule.download.filename.csv");
        int unitMaxSize = Integer.valueOf(properties.getProperty("AuditLogBrowser.schedule.download.unit-maxsize"));
        
        // No backup directory set.
        if(dstFolderPath == null || dstFolderPath.isEmpty()) {
            throw new AlfrescoRuntimeException(MSG_NO_BACKUP_DIRECTORY);
        }

        // from
        LocalDate fromDate = LocalDate.of(2005, 11, 1);    
        // to
        LocalDate toDate = LocalDate.now().minusDays(retentionPeriod);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("============ FromDate: {}", fromDate);
            LOG.debug("============ ToDate: {}", toDate);
        }

        // temporary Directory for CSV
        File tmpDir = fileManager.createDir(sysDefaultPath, dirName);
        // Need to prepare folder for Backup data.
        NodeRef auditRootFolder = repositoryFolderManager.prepareNestedFolder(repositoryFolderManager.getCompanyHomeNodeRef(), dstFolderPath.split("/"));
        LocalDate targetDate = fromDate;

        while(targetDate.isBefore(toDate)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("============ Loop Start {} ============", targetDate);
            }
            
            String targetDateStr = targetDate.format(FORMAT_DATE.withResolverStyle(ResolverStyle.STRICT));
            Long fromEpochMilli  = DateUtil.generateFromEpochMilli(targetDate);
            Long toEpochMilli    = DateUtil.generateToEpochMilli(targetDate);
            
            Long entryId = null;
            String csvName = String.format(csvNameFormat, targetDateStr);
            File csv = csvManager.prepareCSV(tmpDir.getAbsolutePath(), csvName);

            List<Map<String, Object>> auditLogs;
                        
            do {
                
                auditLogs = auditLogManager.getAuditLogs(appName, unitMaxSize, fromEpochMilli, toEpochMilli, entryId, null);
                
                if (auditLogs.isEmpty()) {
                    break;
                }
                
                auditLogs.stream().forEach(entry -> csvManager.addRecord(csv, entry));
                
                // For next query parameter
                entryId = (Long)auditLogs.get(auditLogs.size()-1).get(KEY_ID) + 1;
                
            } while (auditLogs.size() == unitMaxSize);
            
            targetDate = targetDate.plusDays(1);
            
            // If there is only a header line, subsequent processing is not performed.
            if (!csv.exists() || !csvManager.hasRecord(csv)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("============ There is no data found: {} ============", targetDateStr);
                }
                continue;
            }
            
            NodeRef dateFolder = repositoryFolderManager.prepareNestedFolder(auditRootFolder, targetDateStr.split("-"));
            String zipName = String.format(NAME_DAILYZIP, targetDateStr);
            
            if (repositoryFolderManager.isExist(dateFolder, zipName)) {
                continue;
            }
            
            // for Zip
            File zip = new File(tmpDir.getAbsolutePath(), zipName);
            File[] csvs = {csv};
            
            zipManager.createZip(zip, csvs);
            repositoryFolderManager.addContent(dateFolder, zip);
            
            fileManager.deleteAllFiles(tmpDir);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("============ Loop End ============");
            }
            
        }
        
        if(doDelete) {
            this.cleanUp(fromDate, toDate);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("============ Finish Backup process.");
        }

    }
    
    /**
     * 
     * @param fromDate  Delete start DateTime.
     * @param toDate    Delete end DateTime.
     */
    private void cleanUp(LocalDate fromDate, LocalDate toDate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("============ Start Delete process");
            // Even if you delete old logs, there is no problem
            LOG.debug("============ fromDate: {}", fromDate);
            LOG.debug("============ toDate: {}", toDate);
        }
        
        String appName      = properties.getProperty("AuditLogBrowser.schedule.download.appname");        
        Long fromEpochMilli = DateUtil.generateFromEpochMilli(fromDate);
        Long toEpochMilli   = DateUtil.generateToEpochMilli(toDate);
        
        auditLogManager.delete(appName, fromEpochMilli, toEpochMilli);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("============ Finish Delete process");
        }
    }

}
