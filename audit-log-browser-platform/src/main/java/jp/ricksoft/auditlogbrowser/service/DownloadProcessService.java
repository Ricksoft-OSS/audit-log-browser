package jp.ricksoft.auditlogbrowser.service;

import java.util.concurrent.ConcurrentMap;

import jp.ricksoft.auditlogbrowser.service.manager.download.DownloadProcessInfo;
import jp.ricksoft.auditlogbrowser.service.manager.download.DownloadProgress;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import com.google.common.collect.Maps;

public class DownloadProcessService {
    private ConcurrentMap<String, DownloadProcessInfo> dlProcessesInProgress = Maps.newConcurrentMap();

    private static final Logger LOG = LoggerFactory.getLogger(DownloadProcessService.class);

    public NodeRef getZipFileRef(String pid) {
        if (this.dlProcessesInProgress.get(pid) == null) {
            return null;
        }

        return this.dlProcessesInProgress.get(pid).getZipFileRef();
    }

    public void removeProcessInfo(String pid) {
        if (this.dlProcessesInProgress.get(pid) == null) {
            return;
        }

        this.dlProcessesInProgress.remove(pid);
    }

    public int getProgressPercentage(String pid) {
        if (this.dlProcessesInProgress.get(pid) == null) {
            return 0;
        }

        final int total = this.dlProcessesInProgress.get(pid).getTotal();
        final int created = this.dlProcessesInProgress.get(pid).getCreated();

        LOG.debug("DL: {} / {}", created, total);

        if (total == -1) {
            return 0;
        }

        return 100 * created / total;
    }

    public DownloadProgress getProgress(String pid) {
        final DownloadProcessInfo dlInfo = dlProcessesInProgress.get(pid);

        LOG.debug("in-prog.: {}", this.dlProcessesInProgress);

        // When dl-process encounter any unexpected error
        if (dlInfo == null) {
            return DownloadProgress.STATUS_FAILURE;
        }

        // When dl-process is any expected exceptions
        if (dlInfo.isFailed()) {
            return DownloadProgress.STATUS_FAILURE;
        }

        // When dl-process is completed
        if (dlInfo.getZipFileRef() != null) {
            return DownloadProgress.STATUS_FINISHED;
        }

        // Others = in-progress
        return DownloadProgress.STATUS_IN_PROGRESS;
    }

    public void registerDownloadProcess(String pid) {
        final DownloadProcessInfo dlProcess = new DownloadProcessInfo(pid);
        this.dlProcessesInProgress.put(pid, dlProcess);
    }

    public void setProcessIsFailed(String pid) {
        this.dlProcessesInProgress.get(pid).setFailed(true);

    }

    public void setTotalNum(String pid, int total) throws InterruptedException {
        if (!isActiveProcess(pid)) {
            throw new InterruptedException();
        }
        this.dlProcessesInProgress.get(pid).setTotal(total);
    }

    public void addCreatedNum(String pid, int createdNum) throws InterruptedException {
        if (!isActiveProcess(pid)) {
            throw new InterruptedException();
        }
        this.dlProcessesInProgress.get(pid).setCreated(this.dlProcessesInProgress.get(pid).getCreated() + createdNum);
    }

    public void setZipFileRef(String pid, NodeRef zipFileRef) throws InterruptedException {
        if (!isActiveProcess(pid)) {
            throw new InterruptedException();
        }
        this.dlProcessesInProgress.get(pid).setZipFileRef(zipFileRef);
    }

    public boolean isActiveProcess(String pid) {
        return this.dlProcessesInProgress.containsKey(pid);
    }

    public int getTotal(String pid) {
        if (!isActiveProcess(pid)) {
            return 0;
        }

        return this.dlProcessesInProgress.get(pid).getTotal();
    }

    public int getCreated(String pid) {
        if (!isActiveProcess(pid)) {
            return 0;
        }
        return this.dlProcessesInProgress.get(pid).getCreated();
    }

}
