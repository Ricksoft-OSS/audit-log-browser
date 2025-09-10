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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

import java.io.Serializable;

public class DocumentTypeDataExtractor extends AbstractDataExtractor {

    private NodeService nodeService;
    private NamespaceService namespaceService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
    }

    @Override
    public boolean isSupported(Serializable data) {
        return (data instanceof NodeRef);
    }

    @Override
    public Serializable extractData(Serializable in) {
        
        NodeRef nodeRef = (NodeRef) in;
        if (!nodeService.exists(nodeRef)) {
            if (logger.isDebugEnabled()){
                logger.debug("Extractor can't pull value from non-existent node: " + nodeRef);
            }
            return null;
        }
            
        QName docType = nodeService.getType(nodeRef);
        
        return docType.getPrefixedQName(namespaceService).toPrefixString();
    }

}
