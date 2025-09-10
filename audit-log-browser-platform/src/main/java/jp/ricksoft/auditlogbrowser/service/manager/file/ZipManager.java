package jp.ricksoft.auditlogbrowser.service.manager.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

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

import com.google.common.io.Files;

public class ZipManager {
    private static final Logger LOG = LoggerFactory.getLogger(ZipManager.class);

    @Value("${AuditLogBrowser.dir.tmp}")
    private String tmpDirPath;
    @Value("${AuditLogBrowser.zip.name}")
    private String zipName;

    /**
     * Create Zip file
     *
     * @param files Compressed file
     */
    public File copyFilesToZip(File zip, List<File> files) throws IOException {
        LOG.info("Starting Zip prepare.");

        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)))) {
            this.addEntries(zos, files);
            LOG.info("Finished Zip prepare.");
            return zip;
        } catch (IOException e) {
            LOG.error("Fail to prepare Zip.");
            throw e;
        }
    }

    private void addEntries(ZipOutputStream zos, List<File> files) {
        files.forEach(file -> {
            try {
                zos.putNextEntry(new ZipEntry(file.getName()));
                Files.copy(file, zos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Prepare Zip
     *
     * @return created zip file
     * @throws IOException
     */
    public File createBlankZip(String suffix) throws IOException {
        return this.createBlankZip(Paths.get(this.tmpDirPath), suffix);
    }

    public File createBlankZip(Path directoryToCreate, String suffix) throws IOException {
        File zip = new File(directoryToCreate.toFile(), zipName + "_" + suffix + ".zip");
        zip.createNewFile();
        return zip;
    }

}
