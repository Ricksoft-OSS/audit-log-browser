package jp.ricksoft.auditlogbrowser.webscript;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

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
import jp.ricksoft.auditlogbrowser.audit.download.DownloadProgress;

public class ExportStatusWebScript extends DeclarativeWebScript {

    private DownloadAuditLogZipHandler handler;

    public void setHandler(DownloadAuditLogZipHandler handler) {
        this.handler = handler;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>();

        // check mandatory parameter.
        final String pid = req.getParameter("pid");
        final DownloadProgress dlp = handler.getProgress(pid);

        if (pid == null || "".equals(pid)) {
            status.setCode(Status.STATUS_BAD_REQUEST, "pid is Mandatory.");
            status.setRedirect(true);

            return model;
        }

        final NodeRef zipFileRef = handler.getZipFileRef(pid);
        if (zipFileRef != null) {
            handler.removeProcessInfo(pid);
            model.put("zipFileRef", zipFileRef.toString());
        }
        model.put("exportStatus", dlp.message());
        model.put("percentage", handler.getProgressPercentage(pid));

        return model;
    }

}
