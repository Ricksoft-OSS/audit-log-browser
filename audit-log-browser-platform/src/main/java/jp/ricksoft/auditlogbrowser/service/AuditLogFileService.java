package jp.ricksoft.auditlogbrowser.service;

/*-
 * #%L
 * Audit Log Browser Platform JAR Module
 * %%
 * Copyright (C) 2018 - 2024 Ricksoft Co., Ltd.
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

import com.google.common.collect.Lists;
import jp.ricksoft.auditlogbrowser.service.manager.audit.AuditLogManager;
import jp.ricksoft.auditlogbrowser.service.manager.file.CSVManager;
import jp.ricksoft.auditlogbrowser.service.manager.file.ZipManager;
import jp.ricksoft.auditlogbrowser.service.manager.repo.RepositoryFolderManager;
import jp.ricksoft.auditlogbrowser.util.DateTimeUtil;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AuditLogFileService {
    private static final Logger LOG = LoggerFactory.getLogger(AuditLogFileService.class);
    private static final String SUFFIX_ON_DEMAND_FOLDER = "on-demand";

    @Value("${AuditLogBrowser.schedule.backup.directory}")
    private String dstFolderPath;

    private CSVManager csvManager;
    private ZipManager zipManager;
    private AuditLogManager auditLogManager;
    private RepositoryFolderManager repositoryFolderManager;

    private DownloadProcessService downloadProcessManager;
    private FileSystemControlService fileManager;

    public void setCsvManager(CSVManager csvManager) {
        this.csvManager = csvManager;
    }

    public void setZipManager(ZipManager zipManager) {
        this.zipManager = zipManager;
    }

    public void setFileManager(FileSystemControlService fileManager) {
        this.fileManager = fileManager;
    }

    public void setAuditLogManager(AuditLogManager auditLogManager) {
        this.auditLogManager = auditLogManager;
    }

    public void setDownloadProcessManager(DownloadProcessService downloadProcessManager) {
        this.downloadProcessManager = downloadProcessManager;
    }

    public void setRepositoryFolderManager(RepositoryFolderManager repositoryFolderManager) {
        this.repositoryFolderManager = repositoryFolderManager;
    }

    public void exportAuditLogsZipToRepo(String fromDate, String fromTime, String toDate, String toTime,
                                         String user, Map<String, Serializable> searchValues, String pid) {

        // TODO: longを貰えるようにする
        // TODO: 格納先のパス名とファイル名を受け取れるようにする
        final long start = prepareStartEpochMilli(fromDate, fromTime);
        final long end = prepareEndEpochMilli(toDate, toTime);

        this.downloadProcessManager.registerDownloadProcess(pid);

        Path workDir = null;
        File zipFile = null;
        try {
            workDir = this.fileManager.createWorkDirInTmp(pid);

            // NOTE: THIS METHOD CALL IS HEAVY
            // get audit logs csv
            final List<File> createdFileList = this.createAuditLogsZip(start, end, user, searchValues, workDir, pid);

            // zip csv files
            zipFile = zipManager.copyFilesToZip(this.zipManager.createBlankZip(pid), createdFileList);

            // register a zipped files to ACS repo
            final NodeRef zipRefRegistered = this.registerAuditLogsZip(zipFile, new String[]{SUFFIX_ON_DEMAND_FOLDER});

            // finish process and record NodeRef
            this.downloadProcessManager.setZipFileRef(pid, zipRefRegistered);
        } catch (InterruptedException e) {
            LOG.info("Download process is interrupted. PID[{}]", pid);
        } catch (IOException e) {
            this.downloadProcessManager.setProcessIsFailed(pid);
            throw new RuntimeException(e);
        } finally {
            if (zipFile != null && zipFile.exists()) {
                zipFile.delete();
            }

            if (workDir != null && workDir.toFile().exists()) {
                this.fileManager.deleteAllFiles(workDir.toFile());
                workDir.toFile().delete();
            }
        }
    }

    /**
     * Acquire audit log and create csv file.
     *
     * @param user Username
     * @throws InterruptedException
     */
    private List<File> createAuditLogsZip(long start, long end,
                                          String user, Map<String, Serializable> searchValue,
                                          Path workDirPath, String pid)
            throws InterruptedException {

        final List<File> createdFileList = Lists.newArrayList();

        this.downloadProcessManager.setTotalNum(pid, auditLogManager.getTotalAuditEntriesNum(start, end, user,
                searchValue));

        while (start <= end) {
            final long dayEnd = Math.min(DateTimeUtil.getEndOfDateEpochMilli(start), end);
            final int dayTotal = auditLogManager.getTotalAuditEntriesNum(start, dayEnd,
                    user, searchValue);

            if (dayTotal > 0) {
                // Get Daily AuditLogs
                final File auditLogCSV = this.createOneDayAuditLogCSV(
                        this.auditLogManager.buildAuditQueryParameters(
                                start,
                                dayEnd,
                                null,
                                user,
                                searchValue
                        ),
                        String.format(csvManager.getCsvName(), DateTimeUtil.convertEpochMilliToYYYYMMDD(start)),
                        workDirPath, pid);
                if (auditLogCSV != null) {
                    createdFileList.add(auditLogCSV);
                }
            }

            LOG.trace("created zip : {} - {}", new Date(start), new Date(dayEnd));
            start = dayEnd + 1;
        }

        return createdFileList;
    }

    private NodeRef registerAuditLogsZip(File zipFile, String[] repoDirectoryNameTree) {
        final NodeRef auditRootFolder = repositoryFolderManager.prepareNestedFolder(
                repositoryFolderManager.getCompanyHomeNodeRef(), dstFolderPath.split("/"));

        final NodeRef dateFolder = repositoryFolderManager.prepareNestedFolder(auditRootFolder, repoDirectoryNameTree);

        return repositoryFolderManager.addContent(dateFolder, zipFile);
    }

    private File createOneDayAuditLogCSV(AuditQueryParameters auditQueryParameters, String fileName, Path workDirPath, String pid) throws InterruptedException {
        final File csv = csvManager.prepareCSV(fileName, workDirPath);
        if (csv == null) {
            return null;
        }

        Long entryId = null;
        List<Map<String, Object>> auditLogs;

        do {
            auditQueryParameters.setFromId(entryId);
            auditLogs = this.auditLogManager.getAuditLogs(auditQueryParameters);

            if (auditLogs.isEmpty()) {
                return null;
            }

            auditLogs.forEach(entry -> csvManager.addRecord(csv, entry));
            entryId = (long) auditLogs.get(auditLogs.size() - 1).get(CSVManager.KEY_ID) + 1;

            // record progress
            this.downloadProcessManager.addCreatedNum(pid, auditLogs.size());

        } while (auditLogs.size() == 100);

        return csv;
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
