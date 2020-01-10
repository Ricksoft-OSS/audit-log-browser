/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.audit.dataextractor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

public class ContentNameDataExtractor extends AbstractDataExtractor {
    
    private NamespaceService namespaceService;
    
    private static final String PROP_KEY_FIELNAME = "cm:filename";
    private static final String PROP_KEY_MODELNAME = "cm:modelName";
    private static final String PROP_KEY_USERNAME = "cm:userName";
    private static final String PROP_KEY_AUTHORITYNAME = "cm:authorityName";
    private static final String PROP_KEY_THUMBNAILNAME = "cm:thumbnailName";
    private static final String PROP_KEY_STORENAME = "cm:storeName";
    private static final String PROP_KEY_NAME = "cm:name";
    
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
    }

    /**
     * @return true if this extractor can do anything with the data
     */
    @Override
    public boolean isSupported(Serializable data) {
        return (data != null && data instanceof Map);
    }

    /**
     * Extract the site name / id
     *
     * @param in a string containing the site id
     * @return the site id
     * @throws Throwable
     */
    @Override
    public Serializable extractData(Serializable in) throws Throwable {

        // The data type of all arguments are passed in Serializable and italready checked using "isSupported" method.
        @SuppressWarnings("unchecked")
        Map<QName, Serializable> path = (Map<QName, Serializable>)in;
        
        HashMap<String, String> propMap = new HashMap<String, String>();
        path.forEach((key, value) -> {
            propMap.put(convertPrefixedQName(key), convertSerializableToString(value));
        });
        
        if (propMap.containsKey(PROP_KEY_FIELNAME)) {
            return propMap.get(PROP_KEY_FIELNAME);
        } else if (propMap.containsKey(PROP_KEY_MODELNAME)) {
            return propMap.get(PROP_KEY_MODELNAME);
        } else if (propMap.containsKey(PROP_KEY_USERNAME)) {
            return propMap.get(PROP_KEY_USERNAME);
        } else if (propMap.containsKey(PROP_KEY_AUTHORITYNAME)) {
            return propMap.get(PROP_KEY_AUTHORITYNAME);
        } else if (propMap.containsKey(PROP_KEY_THUMBNAILNAME)) {
            return propMap.get(PROP_KEY_THUMBNAILNAME);
        } else if (propMap.containsKey(PROP_KEY_STORENAME)) {
            return propMap.get(PROP_KEY_STORENAME);
        } else if (propMap.containsKey(PROP_KEY_NAME)) {
            return propMap.get(PROP_KEY_NAME);
        } else {
            return "No Content Name.";
        }
        
    }
    
    private String convertPrefixedQName(QName key) {
        return key.getPrefixedQName(namespaceService).toPrefixString();
    }
    
    private String convertSerializableToString(Serializable value) {
        if (value instanceof Object) {
            return value.toString();
        } else {
            return String.valueOf(value);
        }
    }

}
