package jp.ricksoft.auditlogbrowser.audit.download;

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
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Controller
public class DownloadAuditLogZipHandler {

    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_FINISHED = "Finished";
    public static final String STATUS_FAILURE = "Failure";

    private CSVManager csvManager;
    private ZipManager zipManager;
    private FileManager fileManager;
    private AuditLogManager auditLogManager;

    private String progress;
    private String processId;

    public void setCsvManager(CSVManager csvManager) {
        this.csvManager = csvManager;
    }

    public void setZipManager(ZipManager zipManager) {
        this.zipManager = zipManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setAuditLogManager(AuditLogManager auditLogManager)
    {
        this.auditLogManager = auditLogManager;
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


    public void execExport(String fromDate, String fromTime, String toDate, String toTime, String user) {

        this.setProgress(STATUS_IN_PROGRESS);
        this.setProcessId(String.valueOf(UUID.randomUUID()));

        try {
            File zip = zipManager.createBlankZip(processId);
            this.createAuditLogsZip(zip, fromDate, fromTime, toDate, toTime, user);
        } catch (IOException e) {
            this.setProgress(STATUS_FAILURE);
            e.printStackTrace();
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
    private void createAuditLogsZip(File zip, String fromDate, String fromTime, String toDate, String toTime, String user) {

        long start = prepareStartEpochMilli(fromDate, fromTime);

        long end = prepareEndEpochMilli(toDate, toTime);
        long dayEnd;

        while (start <= end)
        {
            dayEnd = Math.min(DateTimeUtil.getEndOfDateEpochMilli(start), end);

            // Get Daily AuditLogs
            csvManager.createOneDayAuditLogCSV(start, dayEnd, user);

            start = dayEnd + 1;

        }

        zipManager.prepareZip(zip, fileManager.getTmpDir().listFiles());

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
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(date).atTime(LocalTime.parse(time)).plusMinutes(1)) - 1;
        }
    }
}
