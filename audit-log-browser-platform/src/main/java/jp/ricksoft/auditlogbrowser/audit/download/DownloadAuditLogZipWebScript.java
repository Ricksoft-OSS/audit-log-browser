/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.audit.download;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.Map;

public class DownloadAuditLogZipWebScript extends DeclarativeWebScript {

    private DownloadAuditLogZipHandler handler;

    public void setDownloadAuditLogZipHandler(DownloadAuditLogZipHandler downloadAuditLogZipHandler) {
        this.handler = downloadAuditLogZipHandler;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        // Acquisition period setting
        String fromDate = req.getParameter("fromDate");
        String fromTime = req.getParameter("fromTime");
        String toDate = req.getParameter("toDate");
        String toTime = req.getParameter("toTime");
        String user = req.getParameter("user");

        handler.execCreateAuditLogsZip(fromDate, fromTime, toDate, toTime, user);

        Map<String, Object> model = new HashMap<>();
        model.put("staatus", "Success");

        return model;
    }

}
