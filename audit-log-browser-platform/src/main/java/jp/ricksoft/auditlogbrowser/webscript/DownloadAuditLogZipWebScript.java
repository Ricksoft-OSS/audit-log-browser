package jp.ricksoft.auditlogbrowser.webscript;

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

import jp.ricksoft.auditlogbrowser.audit.download.DownloadAuditLogZipHandler;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.Map;

public class DownloadAuditLogZipWebScript extends DeclarativeWebScript {

    private DownloadAuditLogZipHandler handler;

    public void setHandler(DownloadAuditLogZipHandler handler) {
        this.handler = handler;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        // Acquisition period setting
        String fromDate = req.getParameter("fromDate");
        String fromTime = req.getParameter("fromTime");
        String toDate = req.getParameter("toDate");
        String toTime = req.getParameter("toTime");
        String user = req.getParameter("user");

        handler.execExport(fromDate, fromTime, toDate, toTime, user);

        Map<String, Object> model = new HashMap<>();
        model.put("exportStatus", handler.getProgress());
        model.put("processId", handler.getProcessId());

        return model;
    }

}
