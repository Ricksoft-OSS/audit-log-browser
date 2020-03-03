package jp.ricksoft.auditlogbrowser.audit.download;

import jp.ricksoft.auditlogbrowser.audit.AuditLogManager;
import jp.ricksoft.auditlogbrowser.file.CSVManager;
import jp.ricksoft.auditlogbrowser.file.ZipManager;
import jp.ricksoft.auditlogbrowser.util.DateTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
public class DownloadAuditLogZipHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadAuditLogZipHandler.class);

    private static String STATUS_IN_PROGRESS = "In Progress";
    private static String STATUS_FINISHED = "Finished";

    private String msgFailCreateZip;
    private CSVManager csvManager;
    private ZipManager zipManager;
    private String progress;

    private AuditLogManager auditLogManager;

    public void setMsgFailCreateZip(String msgFailCreateZip) {
        this.msgFailCreateZip = msgFailCreateZip;
    }

    public void setCsvManager(CSVManager csvManager) {
        this.csvManager = csvManager;
    }

    public void setZipManager(ZipManager zipManager) {
        this.zipManager = zipManager;
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

    @Async
    public void execCreateAuditLogsZip(String fromDate, String fromTime, String toDate, String toTime, String user) {
        LOG.info("Starting Create Audit log CSV.");

        try {

            this.setProgress(STATUS_IN_PROGRESS);

            LOG.info("Set In Progress.");

            this.createAuditLogsCSV(fromDate, fromTime, toDate, toTime, user);

            LOG.info("Exec createAuditLogsCSV.");

            File zip = zipManager.prepareZip();

            // Check whether zip exist.
            if (!zip.exists()) {
                throw new IOException(msgFailCreateZip);
            }

            LOG.info("Finish Create Audit log CSV.");

            this.setProgress(STATUS_FINISHED);

        } catch (IOException e) {
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
    private void createAuditLogsCSV(String fromDate, String fromTime, String toDate, String toTime, String user) {

        long start = prepareStartEpochMilli(fromDate, fromTime);

        long end = prepareEndEpochMilli(toDate, toTime);
        long dayEnd;

        while (start <= end)
        {
            dayEnd = DateTimeUtil.getEndOfDateEpochMilli(start);
            if (dayEnd >= end)
            {
                dayEnd = end;
            }

            // Get Daily AuditLogs
            csvManager.createOneDayAuditLogCSV(start, dayEnd, user);

            start = dayEnd + 1;

        }
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
