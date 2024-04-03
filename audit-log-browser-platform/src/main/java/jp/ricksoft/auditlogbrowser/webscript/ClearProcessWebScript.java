package jp.ricksoft.auditlogbrowser.webscript;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

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

public class ClearProcessWebScript extends AbstractWebScript {

    private DownloadAuditLogZipHandler handler;

    public void setHandler(DownloadAuditLogZipHandler handler) {
        this.handler = handler;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        final String pid = req.getParameter("pid");

        if (pid == null || "".equals(pid)) {
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }
        this.handler.removeProcessInfo(pid);
    }
}
