package jp.ricksoft.auditlogbrowser.audit.download;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Strings;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import jp.ricksoft.auditlogbrowser.NodeRef.RepositoryFolderManager;

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
import jp.ricksoft.auditlogbrowser.file.CSVManager;
import jp.ricksoft.auditlogbrowser.file.FileManager;
import jp.ricksoft.auditlogbrowser.file.ZipManager;
import jp.ricksoft.auditlogbrowser.util.DateTimeUtil;

@Controller
public class DownloadAuditLogZipHandler {

    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_FINISHED = "Finished";
    public static final String STATUS_FAILURE = "Failure";

    private static final String SUFFIX_ON_DEMAND_FOLDER = "on-demand";

    private static final Logger LOG = LoggerFactory.getLogger(DownloadAuditLogZipHandler.class);

    @Value("${AuditLogBrowser.schedule.backup.directory}")
    private String dstFolderPath;

    private CSVManager csvManager;
    private ZipManager zipManager;
    private FileManager fileManager;
    private AuditLogManager auditLogManager;
    private RepositoryFolderManager repositoryFolderManager;

    private String progress;
    private String processId;
    private NodeRef zipFileRef = null;

    public NodeRef getZipFileRef() {
        return zipFileRef;
    }

    public void setCsvManager(CSVManager csvManager) {
        this.csvManager = csvManager;
    }

    public void setZipManager(ZipManager zipManager) {
        this.zipManager = zipManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setAuditLogManager(AuditLogManager auditLogManager) {
        this.auditLogManager = auditLogManager;
    }

    public void setRepositoryFolderManager(RepositoryFolderManager repositoryFolderManager) {
        this.repositoryFolderManager = repositoryFolderManager;
    }

    public String getProgress() {
        return this.progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getProcessId() {
        return this.processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public synchronized void execExport(String fromDate, String fromTime, String toDate, String toTime, String user) {

        this.setProgress(STATUS_IN_PROGRESS);
        this.setProcessId(String.valueOf(UUID.randomUUID()));
        this.zipFileRef = null;

        Path workDir = null;

        try {
            workDir = this.fileManager.createWorkDirInTmp(this.processId);

            final File zip = this.zipManager.createBlankZip(processId);

            this.createAuditLogsZip(zip, fromDate, fromTime, toDate, toTime, user, workDir);
        } catch (IOException e) {
            this.setProgress(STATUS_FAILURE);
            e.printStackTrace();
        } finally {
            if (workDir != null && workDir.toFile().exists()) {
                this.fileManager.deleteAllFiles(workDir.toFile());
                workDir.toFile().delete();
            }
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
    protected void createAuditLogsZip(File zip, String fromDate, String fromTime, String toDate, String toTime,
            String user) {
        this.createAuditLogsZip(zip, fromDate, fromTime, toDate, toTime, user, null);
    }

    @Async
    protected void createAuditLogsZip(File zip, String fromDate, String fromTime, String toDate, String toTime,
            String user, Path workDirPath) {

        final List<File> createdFileList = Lists.newArrayList();
        long start = prepareStartEpochMilli(fromDate, fromTime);
        long end = prepareEndEpochMilli(toDate, toTime);

        while (start <= end) {
            final long dayEnd = Math.min(DateTimeUtil.getEndOfDateEpochMilli(start), end);

            // Get Daily AuditLogs
            final File auditLogCSV = csvManager.createOneDayAuditLogCSV(start, dayEnd, user, workDirPath);
            if (auditLogCSV != null) {
                createdFileList.add(auditLogCSV);
            }
            start = dayEnd + 1;
        }

        final File zipFile = zipManager.prepareZip(zip, createdFileList.toArray(new File[0]));
        if (zipFile == null) {
            this.setProgress(STATUS_FAILURE);
            return;
        }

        try {
            final NodeRef auditRootFolder = repositoryFolderManager
                    .prepareNestedFolder(repositoryFolderManager.getCompanyHomeNodeRef(), dstFolderPath.split("/"));
            final NodeRef dateFolder = repositoryFolderManager.prepareNestedFolder(auditRootFolder,
                    new String[] { SUFFIX_ON_DEMAND_FOLDER });
            this.zipFileRef = repositoryFolderManager.addContent(dateFolder, zip);

        } finally {
            try {
                Files.deleteIfExists(zipFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.setProgress(STATUS_FINISHED);
    }

    private long prepareStartEpochMilli(String date, String time) {
        if (StringUtils.isBlank(date)) {
            return DateTimeUtil.convertEpochMilli(auditLogManager.getOldestLoggedDateTime());
        } else if (StringUtils.isBlank(time)) {
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(date).atStartOfDay());
        } else {
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(date).atTime(LocalTime.parse(time)));
        }
    }

    private long prepareEndEpochMilli(String date, String time) {
        if (StringUtils.isBlank(date)) {
            return DateTimeUtil.convertEpochMilli(LocalDateTime.now());
        } else if (StringUtils.isBlank(time)) {
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(date).plusDays(1).atStartOfDay()) - 1;
        } else {
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(date).atTime(LocalTime.parse(time)).plusMinutes(1))
                    - 1;
        }
    }
}
