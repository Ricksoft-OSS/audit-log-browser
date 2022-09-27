package jp.ricksoft.auditlogbrowser.file;

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

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.stream.Stream;

public class FileManager {

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

}
