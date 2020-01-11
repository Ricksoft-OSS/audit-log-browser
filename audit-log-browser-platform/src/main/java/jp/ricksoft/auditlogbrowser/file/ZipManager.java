/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import com.google.common.io.Files;

public class ZipManager
{

    private Properties properties;

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * Create Zip file
     * 
     * @param zip   Zip file
     * @param files  Compressed file
     */
    public void createZip(File zip, File[] files)
    {
        ZipOutputStream zos = null;
        try
        {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)));
            this.createZip(zos, files);
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            IOUtils.closeQuietly(zos);
        }
    }

    private void createZip(ZipOutputStream zos, File[] files) throws IOException
    {
        InputStream is = null;
        try
        {
            for (File file : files)
            {
                ZipEntry entry = new ZipEntry(file.getName());
                zos.putNextEntry(entry);
                Files.copy(file, zos);
            }

        } finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Prepare Zip
     * 
     * @param folder  temporary directory
     * @return created zip file
     * @throws IOException
     */
    public File prepareZip(File folder) throws IOException
    {
        String tmpDirPath = folder.getAbsolutePath();
        String zipName = properties.getProperty("AuditLogBrowser.schedule.download.filename.zip");

        if (folder.list() == null || folder.list().length < 1)
        {
            return null;
        }

        File zip = new File(tmpDirPath, zipName);
        this.createZip(zip, folder.listFiles());

        return zip;
    }

}
