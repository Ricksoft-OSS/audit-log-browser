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
import jp.ricksoft.auditlogbrowser.audit.download.DownloadProgress;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClearProcessWebScript extends AbstractWebScript {

    private DownloadAuditLogZipHandler handler;

    public void setHandler(DownloadAuditLogZipHandler handler) {
        this.handler = handler;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        final String pid = req.getParameter("pid");
        final DownloadProgress dlp = handler.getProgress(pid);

        if (pid == null || "".equals(pid)) {
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }
        this.handler.removeProcessInfo(pid);
    }
}
