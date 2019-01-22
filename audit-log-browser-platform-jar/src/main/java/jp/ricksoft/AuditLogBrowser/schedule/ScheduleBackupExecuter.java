/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.AuditLogBrowser.schedule;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleBackupExecuter extends ActionExecuterAbstractBase {
	
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleBackupExecuter.class); 
    public static final String PARAM_SIMPLE = "simpleParam";
    
    private NodeService nodeService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
    }
    
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        String simpleParam = (String) action.getParameterValue(PARAM_SIMPLE);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Repository Action called from Scheduled Job, [" + PARAM_SIMPLE + "=" + simpleParam + "]");
        }

        if (nodeService.exists(actionedUponNodeRef)) {
            // Start Repository Action Implementation.
            String nodeName = (String)nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Simple Repo Action invoked on node [name=" + nodeName + "]");
            }
        }
    }

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
	    paramList.add(new ParameterDefinitionImpl(
	        PARAM_SIMPLE,
                DataTypeDefinition.TEXT,
                true,
                getParamDisplayLabel(PARAM_SIMPLE))
	    );
	}

}
