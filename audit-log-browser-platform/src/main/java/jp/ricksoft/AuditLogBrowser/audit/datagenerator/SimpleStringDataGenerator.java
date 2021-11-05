/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.AuditLogBrowser.audit.datagenerator;

import org.alfresco.repo.audit.generator.AbstractDataGenerator;

import java.io.Serializable;

/**
 * Generate simple string.
 */
public class SimpleStringDataGenerator extends AbstractDataGenerator
{

    private String innerString = "";

    public void setInnerString(String innerString)
    {
        this.innerString = innerString;
    }

    @Override
    public Serializable getData() throws Throwable
    {
        return innerString;
    }

}
