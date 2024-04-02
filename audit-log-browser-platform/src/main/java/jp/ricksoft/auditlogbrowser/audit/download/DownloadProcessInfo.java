package jp.ricksoft.auditlogbrowser.audit.download;

/*-
 * #%L
 * Audit Log Browser Platform JAR Module
 * %%
 * Copyright (C) 2018 - 2024 Ricksoft Co., Ltd.
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

import org.alfresco.service.cmr.repository.NodeRef;

public class DownloadProcessInfo {
    private String processId;
    private NodeRef zipFileRef;
    private boolean failed;

    public DownloadProcessInfo(String processId) {
        this.processId = processId;
        this.failed = false;
    }

    public String getProcessId() {
        return processId;
    }

    public NodeRef getZipFileRef() {
        return zipFileRef;
    }

    public void setZipFileRef(NodeRef zipFileRef) {
        this.zipFileRef = zipFileRef;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public String toString() {
        return processId + ":" + zipFileRef + ":" + String.valueOf(failed);
    }
}
