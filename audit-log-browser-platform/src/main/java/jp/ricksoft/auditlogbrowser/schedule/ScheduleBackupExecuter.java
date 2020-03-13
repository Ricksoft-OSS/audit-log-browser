package jp.ricksoft.auditlogbrowser.schedule;

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

import java.util.List;

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
