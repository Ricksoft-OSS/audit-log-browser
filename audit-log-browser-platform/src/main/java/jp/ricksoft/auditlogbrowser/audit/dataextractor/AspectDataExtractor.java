/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.audit.dataextractor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

public class AspectDataExtractor extends AbstractDataExtractor {
    
    private NamespaceService namespaceService;
    
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
    }

    @Override
    public boolean isSupported(Serializable data) {
        return (data != null && data instanceof Set);
    }

    @Override
    public Serializable extractData(Serializable in) throws Throwable {
        
        if (logger.isDebugEnabled()) {
            logger.debug("AspectLogInput： " + in);
        }
        
        // The data type of all arguments are passed in Serializable and italready checked using "isSupported" method.
        @SuppressWarnings("unchecked")
        HashSet<QName> qNames = (HashSet<QName>) in;
        
        return qNames.stream()
                     .map(item -> item.getPrefixedQName(namespaceService))
                     .map(item -> item.toPrefixString())
                     .collect(Collectors.joining(","));
        
    }

}
