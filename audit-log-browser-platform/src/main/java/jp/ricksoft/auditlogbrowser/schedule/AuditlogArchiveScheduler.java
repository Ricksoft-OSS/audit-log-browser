package jp.ricksoft.auditlogbrowser.schedule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.UUID;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

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

import jp.ricksoft.auditlogbrowser.NodeRef.RepositoryFolderManager;
import jp.ricksoft.auditlogbrowser.audit.AuditLogManager;
import jp.ricksoft.auditlogbrowser.file.CSVManager;
import jp.ricksoft.auditlogbrowser.file.FileManager;
import jp.ricksoft.auditlogbrowser.file.ZipManager;
import jp.ricksoft.auditlogbrowser.util.DateUtil;

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

        final String processId = String.valueOf(UUID.randomUUID());
        Path workDir = null;

        try {
            // No backup directory set.
            if (dstFolderPath == null || dstFolderPath.isEmpty()) {
                throw new AlfrescoRuntimeException(MSG_NO_BACKUP_DIRECTORY);
            }

            workDir = fileManager.createWorkDirInTmp(processId);

            // from
            LocalDate fromDate = auditLogManager.getOldestLoggedDateTime().toLocalDate();
            // to
            LocalDate toDate = LocalDate.now().minusDays(retentionPeriod);

            LOG.debug("============ FromDate: {}", fromDate);
            LOG.debug("============ ToDate: {}", toDate);

            // Need to prepare folder for Backup data.
            NodeRef auditRootFolder = repositoryFolderManager
                    .prepareNestedFolder(repositoryFolderManager.getCompanyHomeNodeRef(), dstFolderPath.split("/"));
            LocalDate targetDate = fromDate;

            while (targetDate.isBefore(toDate)) {
                LOG.debug("============ Loop Start {} ============", targetDate);

                String targetDateStr = targetDate.format(FORMAT_DATE.withResolverStyle(ResolverStyle.STRICT));
                long fromEpochMilli = DateUtil.generateFromEpochMilli(targetDate);
                long toEpochMilli = DateUtil.generateToEpochMilli(targetDate);

                File csv = csvManager.createOneDayAuditLogCSV(fromEpochMilli, toEpochMilli, null, null, workDir);

                targetDate = targetDate.plusDays(1);

                // If there is only a header line, subsequent processing is not performed.
                if (csv == null || !csv.exists() || !csvManager.hasRecord(csv)) {
                    LOG.debug("============ There is no data found: {} ============", targetDateStr);
                    continue;
                }

                NodeRef dateFolder = repositoryFolderManager.prepareNestedFolder(auditRootFolder,
                        targetDateStr.split("-"));
                String zipName = String.format(NAME_DAILYZIP, targetDateStr);

                if (repositoryFolderManager.isExist(dateFolder, zipName)) {
                    continue;
                }

                // for Zip
                File[] csvs = { csv };

                File zip = zipManager.createBlankZip(workDir, targetDateStr);
                zipManager.prepareZip(zip, csvs);
                repositoryFolderManager.addContent(dateFolder, zip);

                LOG.debug("============ Loop End ============");

            }

            if (isDeleteEnabled) {
                this.cleanUp(fromDate, toDate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (workDir != null && workDir.toFile().exists()) {
                this.fileManager.deleteAllFiles(workDir.toFile());
                workDir.toFile().delete();
            }
        }

        LOG.debug("============ Finish Schedule Archive.");

    }

    /**
     * 
     * @param fromDate Delete start DateTime.
     * @param toDate   Delete end DateTime.
     */
    private void cleanUp(LocalDate fromDate, LocalDate toDate) {
        LOG.debug("============ Delete old audit log start");
        // Even if you delete old logs, there is no problem
        LOG.debug("============ fromDate: {}", fromDate);
        LOG.debug("============ toDate: {}", toDate);

        long fromEpochMilli = DateUtil.generateFromEpochMilli(fromDate);
        long toEpochMilli = DateUtil.generateToEpochMilli(toDate);

        auditLogManager.delete(fromEpochMilli, toEpochMilli);

        LOG.debug("============ Delete old audit log end");
    }
}
