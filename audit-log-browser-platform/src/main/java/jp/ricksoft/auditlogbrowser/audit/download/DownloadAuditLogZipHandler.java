package jp.ricksoft.auditlogbrowser.audit.download;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

    private static final String SUFFIX_ON_DEMAND_FOLDER = "on-demand";

    private static final Logger LOG = LoggerFactory.getLogger(DownloadAuditLogZipHandler.class);

    @Value("${AuditLogBrowser.schedule.backup.directory}")
    private String dstFolderPath;

    private CSVManager csvManager;
    private ZipManager zipManager;
    private FileManager fileManager;
    private AuditLogManager auditLogManager;
    private RepositoryFolderManager repositoryFolderManager;

    private ConcurrentMap<String, DownloadProcessInfo> dlProcessesInProgress = Maps.newConcurrentMap();

    public NodeRef getZipFileRef(String pid) {
        if (this.dlProcessesInProgress.get(pid) == null) {
            return null;
        }

        return this.dlProcessesInProgress.get(pid).getZipFileRef();
    }

    public void removeProcessInfo(String pid) {
        if (this.dlProcessesInProgress.get(pid) == null) {
            return;
        }

        this.dlProcessesInProgress.remove(pid);
    }

    public int getProgressPercentage(String pid) {
        if (this.dlProcessesInProgress.get(pid) == null) {
            return 0;
        }

        final int total = this.dlProcessesInProgress.get(pid).getTotal();
        final int created = this.dlProcessesInProgress.get(pid).getCreated();

        LOG.debug("DL: {} / {}", created, total);

        if (total == -1) {
            return 0;
        }

        return 100 * created / total;
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

    public DownloadProgress getProgress(String pid) {
        final DownloadProcessInfo dlInfo = dlProcessesInProgress.get(pid);

        LOG.debug("in-prog.: {}", this.dlProcessesInProgress);

        // When dl-process encounter any unexpected error
        if (dlInfo == null) {
            return DownloadProgress.STATUS_FAILURE;
        }

        // When dl-process is any expected exceptions
        if (dlInfo.isFailed()) {
            return DownloadProgress.STATUS_FAILURE;
        }

        // When dl-process is completed
        if (dlInfo.getZipFileRef() != null) {
            return DownloadProgress.STATUS_FINISHED;
        }

        // Others = in-progress
        return DownloadProgress.STATUS_IN_PROGRESS;
    }

    public void execExport(String fromDate, String fromTime, String toDate, String toTime,
            String user, Map<String, Serializable> searchValues, String pid) {

        final DownloadProcessInfo dlProcess = new DownloadProcessInfo(pid);
        this.dlProcessesInProgress.put(pid, dlProcess);

        Path workDir = null;
        try {
            workDir = this.fileManager.createWorkDirInTmp(pid);
            final File zip = this.zipManager.createBlankZip(pid);

            this.createAuditLogsZip(zip, fromDate, fromTime, toDate, toTime, user, searchValues, workDir,
                    dlProcess);
        } catch (IOException e) {
            dlProcess.setFailed(true);
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOG.info("Downloading process is interrupted. PID[{}]", pid);
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
     * @throws InterruptedException
     */
    @Async
    protected void createAuditLogsZip(File zip, String fromDate, String fromTime, String toDate,
            String toTime, String user, Map<String, Serializable> searchValue, Path workDirPath,
            DownloadProcessInfo processInfo)
            throws InterruptedException {

        final List<File> createdFileList = Lists.newArrayList();
        long start = prepareStartEpochMilli(fromDate, fromTime);
        long end = prepareEndEpochMilli(toDate, toTime);

        this.threadSafeAccessToProgressContainer(processInfo.getProcessId())
                .setTotal(auditLogManager.getTotalAuditEntriesNum(start, end, user,
                        searchValue));

        while (start <= end) {
            final long dayEnd = Math.min(DateTimeUtil.getEndOfDateEpochMilli(start), end);
            final int dayTotal = auditLogManager.getTotalAuditEntriesNum(start, dayEnd,
                    user, searchValue);

            if (dayTotal > 0) {
                // Get Daily AuditLogs
                final File auditLogCSV = csvManager.createOneDayAuditLogCSV(start, dayEnd, user, searchValue,
                        workDirPath);
                if (auditLogCSV != null) {
                    createdFileList.add(auditLogCSV);
                }

                this.threadSafeAccessToProgressContainer(processInfo.getProcessId())
                        .addCreatedNum(dayTotal);
            }

            LOG.trace("created zip : {} - {}", new Date(start), new Date(dayEnd));
            start = dayEnd + 1;
        }

        final File zipFile = zipManager.prepareZip(zip, createdFileList.toArray(new File[0]));
        if (zipFile == null) {
            processInfo.setFailed(true);
            return;
        }

        try {
            final NodeRef auditRootFolder = repositoryFolderManager.prepareNestedFolder(
                    repositoryFolderManager.getCompanyHomeNodeRef(), dstFolderPath.split("/"));
            final NodeRef dateFolder = repositoryFolderManager.prepareNestedFolder(auditRootFolder,
                    new String[] { SUFFIX_ON_DEMAND_FOLDER });
            processInfo.setZipFileRef(repositoryFolderManager.addContent(dateFolder, zip));
        } finally {
            try {
                Files.deleteIfExists(zipFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private DownloadProcessInfo threadSafeAccessToProgressContainer(String pid)
            throws InterruptedException {
        if (this.dlProcessesInProgress.get(pid) == null) {
            throw new InterruptedException();
        }

        return this.dlProcessesInProgress.get(pid);

    }

    private long prepareStartEpochMilli(String date, String time) {
        if (StringUtils.isBlank(date)) {
            return DateTimeUtil.convertEpochMilli(auditLogManager.getOldestLoggedDateTime());
        } else if (StringUtils.isBlank(time)) {
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(date).atStartOfDay());
        } else {
            return DateTimeUtil
                    .convertEpochMilli(LocalDate.parse(date).atTime(LocalTime.parse(time)));
        }
    }

    private long prepareEndEpochMilli(String date, String time) {
        if (StringUtils.isBlank(date)) {
            return DateTimeUtil.convertEpochMilli(LocalDateTime.now());
        } else if (StringUtils.isBlank(time)) {
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(date).plusDays(1).atStartOfDay())
                    - 1;
        } else {
            return DateTimeUtil.convertEpochMilli(
                    LocalDate.parse(date).atTime(LocalTime.parse(time)).plusMinutes(1)) - 1;
        }
    }
}
