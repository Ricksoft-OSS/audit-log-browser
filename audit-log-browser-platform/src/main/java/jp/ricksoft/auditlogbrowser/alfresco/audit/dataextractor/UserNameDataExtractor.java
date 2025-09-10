package jp.ricksoft.auditlogbrowser.alfresco.audit.dataextractor;

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

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserNameDataExtractor extends AbstractDataExtractor {

    private NamespaceService namespaceService;

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
        return (data instanceof Map);
    }

    /**
     * Extract the site name / id
     *
     * @param in a string containing the site id
     * @return the site id
     */
    @Override
    public Serializable extractData(Serializable in) {

        // The data type of all arguments are passed in Serializable and italready checked using "isSupported" method.
        @SuppressWarnings("unchecked")
        Map<QName, Serializable> path = (Map<QName, Serializable>)in;
        
        HashMap<String, String> propMap = new HashMap<>();
        path.forEach((key, value) -> propMap.put(convertPrefixedQName(key), convertSerializableToString(value)));

        return propMap.entrySet().stream()
                                .filter(e -> e.getKey().equals("cm:userName"))
                                .findFirst()
                                .get().getValue();
        
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
