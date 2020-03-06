/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.file;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    public File createZip(File[] files) {
        LOG.info("Starting Zip create.");

        File zip = new File(tmpDirPath, zipName);
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)))) {
            this.addEntries(zos, files);
            LOG.info("Finished Zip create.");
            return zip;
        } catch (IOException e) {
            LOG.error("Fail to create Zip.");
            e.printStackTrace();
            return null;
        }
    }


    private void addEntries(ZipOutputStream zos, File[] files)
    {
        Stream.of(files)
                .forEach(file -> {
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
    public File prepareZip() {
        File tmpDir = new File(tmpDirPath);

        if (tmpDir.list() == null || tmpDir.list().length < 1) {
            return null;
        }

        return this.createZip(tmpDir.listFiles());
    }

}
