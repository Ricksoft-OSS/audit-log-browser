package jp.ricksoft.auditlogbrowser.audit.download;

import jp.ricksoft.auditlogbrowser.file.CSVManager;
import jp.ricksoft.auditlogbrowser.file.ZipManager;
import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.IOException;

@Configuration
public class DownloadAuditLogZipHandler {

    private String msgFailCreateZip;
    private CSVManager csvManager;
    private ZipManager zipManager;

    public void setMsgFailCreateZip(String msgFailCreateZip) {
        this.msgFailCreateZip = msgFailCreateZip;
    }

    public void setCsvManager(CSVManager csvManager) {
        this.csvManager = csvManager;
    }

    public void setZipManager(ZipManager zipManager) {
        this.zipManager = zipManager;
    }

    @Async
    public void execCreateAuditLogsZip(String fromDate, String fromTime, String toDate, String toTime, String user) {

        try {

            csvManager.createAuditLogsCSV(fromDate, fromTime, toDate, toTime, user);

            File zip = zipManager.prepareZip();

            // Check whether zip exist.
            if (!zip.exists()) {
                throw new IOException(msgFailCreateZip);
            }

        } catch (IOException e) {
            throw new AlfrescoRuntimeException(e.getMessage());
        }
    }
}
