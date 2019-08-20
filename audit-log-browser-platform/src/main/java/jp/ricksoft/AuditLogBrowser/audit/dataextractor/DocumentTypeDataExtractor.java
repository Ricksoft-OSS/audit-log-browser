/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.AuditLogBrowser.audit.dataextractor;

import java.io.Serializable;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

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
        return (data != null && data instanceof NodeRef);
    }

    @Override
    public Serializable extractData(Serializable in) throws Throwable {
        
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
