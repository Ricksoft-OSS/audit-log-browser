package jp.ricksoft.auditlogbrowser.alfresco.webscript;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

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

public class ClearProcessWebScript extends AbstractWebScript {

    private static final Logger LOG = LoggerFactory.getLogger(ClearProcessWebScript.class);

    private DownloadProcessService downloadProcessManager;

    public void setDownloadProcessManager(DownloadProcessService downloadProcessManager) {
        this.downloadProcessManager = downloadProcessManager;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        final String pid = req.getParameter("pid");

        if (pid == null || "".equals(pid)) {
            LOG.error("pid is mandatory");
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }
        this.downloadProcessManager.removeProcessInfo(pid);
    }
}
