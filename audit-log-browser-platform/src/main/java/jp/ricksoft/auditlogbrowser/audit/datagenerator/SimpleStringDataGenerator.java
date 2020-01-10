/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.audit.datagenerator;

import java.io.Serializable;

import org.alfresco.repo.audit.generator.AbstractDataGenerator;

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
