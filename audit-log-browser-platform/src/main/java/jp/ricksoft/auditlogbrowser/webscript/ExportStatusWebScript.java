/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.webscript;

import jp.ricksoft.auditlogbrowser.audit.download.DownloadAuditLogZipHandler;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.Map;

public class ExportStatusWebScript extends DeclarativeWebScript {

    private DownloadAuditLogZipHandler handler;

    public void setHandler(DownloadAuditLogZipHandler handler) {
        this.handler = handler;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>();
        model.put("exportStatus", handler.getProgress());
        model.put("processId", handler.getProcessId());

        return model;
    }

}
