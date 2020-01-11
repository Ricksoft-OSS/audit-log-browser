/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.schedule;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.ricksoft.auditlogbrowser.NodeRef.RepositoryFolderManager;
import jp.ricksoft.auditlogbrowser.audit.AuditLogManager;
import jp.ricksoft.auditlogbrowser.file.CSVManager;
import jp.ricksoft.auditlogbrowser.file.FileManager;
import jp.ricksoft.auditlogbrowser.file.ZipManager;
import jp.ricksoft.auditlogbrowser.util.DateUtil;

public class AuditlogArchiveScheduler {

    private static final String KEY_ID = "id";
    private static final String NAME_DAILYZIP = "Auditlogs_%s.zip";
    private static final String MSG_NO_BACKUP_DIRECTORY = "No backup directory set.";
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Logger LOG = LoggerFactory.getLogger(AuditlogArchiveScheduler.class);

    private boolean doDelete;
    private int retentionPeriod;
    private String dstFolderPath;

    private AuditLogManager auditLogManager;
    private CSVManager csvManager;
    private ZipManager zipManager;
    private RepositoryFolderManager repositoryFolderManager;
    private FileManager fileManager;

    public void setDoDelete(String doDelete) {
        this.doDelete = Boolean.parseBoolean(doDelete);
    }

    public void setRetentionPeriod(String retentionPeriod) {
        this.retentionPeriod = Integer.parseInt(retentionPeriod);
    }

    public void setDstFolderPath(String dstFolderPath) {
        this.dstFolderPath = dstFolderPath;
    }
    
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
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("============ Start Schedule Job.");
        }
        
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
        File tmpDir = fileManager.prepareTmpDir();
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

            File csv = csvManager.createOneDayAuditLogCSV(targetDateStr, fromEpochMilli, toEpochMilli, null, tmpDir);
            
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

        Long fromEpochMilli = DateUtil.generateFromEpochMilli(fromDate);
        Long toEpochMilli   = DateUtil.generateToEpochMilli(toDate);
        
        auditLogManager.delete(fromEpochMilli, toEpochMilli);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("============ Finish Delete process");
        }
    }
}
