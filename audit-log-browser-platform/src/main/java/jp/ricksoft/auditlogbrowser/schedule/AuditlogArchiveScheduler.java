/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.schedule;

import jp.ricksoft.auditlogbrowser.NodeRef.RepositoryFolderManager;
import jp.ricksoft.auditlogbrowser.audit.AuditLogManager;
import jp.ricksoft.auditlogbrowser.file.CSVManager;
import jp.ricksoft.auditlogbrowser.file.FileManager;
import jp.ricksoft.auditlogbrowser.file.ZipManager;
import jp.ricksoft.auditlogbrowser.util.DateUtil;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class AuditlogArchiveScheduler {

    private static final String NAME_DAILYZIP = "Auditlogs_%s.zip";
    private static final String MSG_NO_BACKUP_DIRECTORY = "No backup directory set.";
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Logger LOG = LoggerFactory.getLogger(AuditlogArchiveScheduler.class);

    @Value("${AuditLogBrowser.schedule.delete.enabled}")
    private boolean isDeleteEnabled;
    @Value("${AuditLogBrowser.schedule.archive.storage.period}")
    private int retentionPeriod;
    @Value("${AuditLogBrowser.schedule.backup.directory}")
    private String dstFolderPath;

    private AuditLogManager auditLogManager;
    private CSVManager csvManager;
    private ZipManager zipManager;
    private RepositoryFolderManager repositoryFolderManager;
    private FileManager fileManager;
    
    public void setAuditLogManager(AuditLogManager auditLogManager) {
        this.auditLogManager = auditLogManager;
    }
    
    public void setCsvManager(CSVManager csvManager) {
        this.csvManager = csvManager;
    }
    
    public void setZipManager(ZipManager zipManager) {
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
    public void execute() {

        LOG.debug("============ Start Schedule Archive.");
        
        // No backup directory set.
        if(dstFolderPath == null || dstFolderPath.isEmpty()) {
            throw new AlfrescoRuntimeException(MSG_NO_BACKUP_DIRECTORY);
        }

        // from
        LocalDate fromDate = auditLogManager.getOldestLoggedDateTime().toLocalDate();
        // to
        LocalDate toDate = LocalDate.now().minusDays(retentionPeriod);

        LOG.debug("============ FromDate: {}", fromDate);
        LOG.debug("============ ToDate: {}", toDate);

        // Need to prepare folder for Backup data.
        NodeRef auditRootFolder = repositoryFolderManager.prepareNestedFolder(repositoryFolderManager.getCompanyHomeNodeRef(), dstFolderPath.split("/"));
        LocalDate targetDate = fromDate;

        while(targetDate.isBefore(toDate)) {
            LOG.debug("============ Loop Start {} ============", targetDate);
            
            String targetDateStr = targetDate.format(FORMAT_DATE.withResolverStyle(ResolverStyle.STRICT));
            long fromEpochMilli  = DateUtil.generateFromEpochMilli(targetDate);
            long toEpochMilli    = DateUtil.generateToEpochMilli(targetDate);

            File csv = csvManager.createOneDayAuditLogCSV(fromEpochMilli, toEpochMilli, null);
            
            targetDate = targetDate.plusDays(1);
            
            // If there is only a header line, subsequent processing is not performed.
            if (!csv.exists() || !csvManager.hasRecord(csv)) {
                LOG.debug("============ There is no data found: {} ============", targetDateStr);
                continue;
            }

            NodeRef dateFolder = repositoryFolderManager.prepareNestedFolder(auditRootFolder, targetDateStr.split("-"));
            String zipName = String.format(NAME_DAILYZIP, targetDateStr);

            if (repositoryFolderManager.isExist(dateFolder, zipName)) {
                continue;
            }

            // for Zip
            File[] csvs = {csv};

            File zip = zipManager.createZip(csvs);
            repositoryFolderManager.addContent(dateFolder, zip);

            fileManager.cleanupTmpDir();

            LOG.debug("============ Loop End ============");

        }

        if (isDeleteEnabled) {
            this.cleanUp(fromDate, toDate);
        }

        LOG.debug("============ Finish Schedule Archive.");

    }
    
    /**
     * 
     * @param fromDate  Delete start DateTime.
     * @param toDate    Delete end DateTime.
     */
    private void cleanUp(LocalDate fromDate, LocalDate toDate) {
        LOG.debug("============ Delete old audit log start");
        // Even if you delete old logs, there is no problem
        LOG.debug("============ fromDate: {}", fromDate);
        LOG.debug("============ toDate: {}", toDate);

        long fromEpochMilli = DateUtil.generateFromEpochMilli(fromDate);
        long toEpochMilli   = DateUtil.generateToEpochMilli(toDate);
        
        auditLogManager.delete(fromEpochMilli, toEpochMilli);

        LOG.debug("============ Delete old audit log end");
    }
}
