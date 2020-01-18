/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.file;

import com.google.common.io.Files;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipManager {
    private String tmpDirPath;
    private String zipName;

    public void setTmpDirPath(String tmpDirPath) {
        this.tmpDirPath = tmpDirPath;
    }

    public void setZipName(String zipName) {
        this.zipName = zipName;
    }

    /**
     * Create Zip file
     *
     * @param files Compressed file
     */
    public File createZip(File[] files) {
        File zip = new File(tmpDirPath, zipName);
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)))) {
            this.createZip(zos, files);
            return zip;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void createZip(ZipOutputStream zos, File[] files) throws IOException
    {
        for (File file : files)
        {
            ZipEntry entry = new ZipEntry(file.getName());
            zos.putNextEntry(entry);
            Files.copy(file, zos);
        }
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
