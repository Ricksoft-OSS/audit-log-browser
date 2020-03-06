/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.file;

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

}
