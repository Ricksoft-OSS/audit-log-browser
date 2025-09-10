package jp.ricksoft.auditlogbrowser.service;

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

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FileSystemControlService {

    @Value("${AuditLogBrowser.dir.tmp}")
    private String tmpDirPath;

    /**
     * Delete the file/folder. In the case of a folder, delete all files in folder.
     *
     * @param target
     */
    public void deleteAllFiles(File target) {

        // Existance check
        if (target == null || !target.exists()) {
            return;
        }

        if (target.isFile()) {
            target.delete();
        } else if (target.isDirectory()) {
            Stream.of(target.listFiles()).forEach(t -> t.delete());
        }
    }

    public void cleanupTmpDir() {
        File tmpDir = new File(tmpDirPath);
        this.deleteAllFiles(tmpDir);
    }

    public File getTmpDir() {
        return new File(tmpDirPath);
    }

    public Path createWorkDirInTmp(String identifier) {
        File tmpDir = new File(this.tmpDirPath);
        if (!tmpDir.exists() || !tmpDir.isDirectory()) {
            throw new AlfrescoRuntimeException("Temporally directory is missing. Please check directory is existing.");
        }

        Path workingDir = Paths.get(tmpDir.getAbsolutePath(), identifier);
        if (!workingDir.toFile().exists()) {
            try {
                Files.createDirectory(workingDir);
            } catch (IOException e) {
                throw new AlfrescoRuntimeException("Working directory for creating audit zip cannot be created. Please check permissions of temporally directory.", e);
            }
        }
        return workingDir;
    }

}
