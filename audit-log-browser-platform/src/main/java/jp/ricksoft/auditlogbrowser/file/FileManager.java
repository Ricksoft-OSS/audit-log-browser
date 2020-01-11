/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.file;

import java.io.File;
import java.util.Properties;
import java.util.stream.Stream;

public class FileManager
{

    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * @param  parent   Parent folder path
     * @param  dirName  Directory Name
     * @return directory
     */
    public File createDir(String parent, String dirName)
    {
        File dir = new File(parent, dirName);

        if (!dir.exists())
        {
            dir.mkdir();
        }

        return dir;
    }

    /**
     * Delete the file/folder. In the case of a folder, delete all files in folder.
     * 
     * @param target
     */
    public void deleteAllFiles(File target)
    {

        // Existance check
        if (target == null || !target.exists())
        {
            return;
        }

        if (target.isFile())
        {
            target.delete();
        } else if (target.isDirectory())
        {
            Stream.of(target.listFiles()).forEach(t -> t.delete());
        }
    }

    public File prepareTmpDir() {
        return this.createDir(System.getProperty("java.io.tmpdir"), properties.getProperty("AuditLogBrowser.schedule.download.directoryname.tmp"));
    }

}
