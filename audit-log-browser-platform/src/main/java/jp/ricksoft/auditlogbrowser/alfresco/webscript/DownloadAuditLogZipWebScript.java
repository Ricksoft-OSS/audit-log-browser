package jp.ricksoft.auditlogbrowser.alfresco.webscript;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.google.common.collect.Maps;

import jp.ricksoft.auditlogbrowser.service.DownloadProcessService;

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

import jp.ricksoft.auditlogbrowser.service.manager.download.DownloadProgress;
import jp.ricksoft.auditlogbrowser.service.AuditLogFileService;

public class DownloadAuditLogZipWebScript extends DeclarativeWebScript {

    private DownloadProcessService downloadProcessManager;
    private AuditLogFileService auditLogFileService;

    private static final Logger LOG = LoggerFactory.getLogger(DownloadAuditLogZipWebScript.class);

    public void setDownloadProcessManager(DownloadProcessService downloadProcessManager) {
        this.downloadProcessManager = downloadProcessManager;
    }

    public void setAuditLogFileService(AuditLogFileService auditLogFileService) {
        this.auditLogFileService = auditLogFileService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        // Acquisition period setting
        String fromDate = req.getParameter("fromDate");
        String fromTime = req.getParameter("fromTime");
        String toDate = req.getParameter("toDate");
        String toTime = req.getParameter("toTime");
        String user = req.getParameter("createdByUser");
        String pid = req.getParameter("pid");
        String valuesKey = req.getParameter("valuesKey");
        String valuesValue = req.getParameter("valuesValue");

        final Object body = req.parseContent();
        if (body instanceof JSONObject) {
            final JSONObject bodyJson = (JSONObject) body;
            final Set<String> keys = bodyJson.keySet();

            fromDate = keys.contains("fromDate") ? bodyJson.getString("fromDate") : fromDate;
            fromTime = keys.contains("fromTime") ? bodyJson.getString("fromTime") : fromTime;
            toDate = keys.contains("toDate") ? bodyJson.getString("toDate") : toDate;
            toTime = keys.contains("toTime") ? bodyJson.getString("toTime") : toTime;
            user = keys.contains("createdByUser") ? bodyJson.getString("createdByUser") : user;
            pid = keys.contains("pid") ? bodyJson.getString("pid") : pid;
            valuesKey = keys.contains("valuesKey") ? bodyJson.getString("valuesKey") : valuesKey;
            valuesValue = keys.contains("valuesValue") ? bodyJson.getString("valuesValue") : valuesValue;
        }

        final Map<String, Object> model = Maps.newHashMap();
        final Map<String, Serializable> searchValues = Maps.newHashMap();
        // check mandatory parameter.
        if (pid == null || "".equals(pid)) {
            status.setCode(Status.STATUS_BAD_REQUEST, "pid is Mandatory.");
            status.setRedirect(true);

            return model;
        }

        if (valuesKey != null && valuesValue != null) {
            searchValues.put(valuesKey, valuesValue);
        }

        if (!DownloadProgress.STATUS_IN_PROGRESS.equals(downloadProcessManager.getProgress(pid))) {
            auditLogFileService.exportAuditLogsZipToRepo(fromDate, fromTime, toDate, toTime, user, searchValues, pid);
        }

        model.put("exportStatus", downloadProcessManager.getProgress(pid).message());

        return model;
    }

}
