package jp.ricksoft.auditlogbrowser.NodeRef;

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
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;


public class RepositoryFolderManager {

    private NodeLocatorService nodeLocatorService;
    private FileFolderService fileFolderService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeLocatorService = serviceRegistry.getNodeLocatorService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
    }
    
    /**
     * Get repository root.
     * @return
     */
    public NodeRef getCompanyHomeNodeRef() {
        return nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
    }
    
    /**
     * prepare folder.
     */
    public NodeRef prepareFolder(NodeRef parent, String folderName) {
        NodeRef result = fileFolderService.searchSimple(parent, folderName);

        if (result == null || !fileFolderService.exists(result)) {
            return fileFolderService.create(parent, folderName, ContentModel.TYPE_FOLDER).getNodeRef();
        } else {
            return result;
        }

    }
    
    /**
     * Add content to folder
     */
    public void addContent(NodeRef folder, File file) {
        NodeRef zip = fileFolderService.create(folder, file.getName(), ContentModel.TYPE_CONTENT).getNodeRef();
        fileFolderService.getWriter(zip).putContent(file);
    }
    
    /**
     * Check if file exist in folder.
     * @param folderNode  Search place
     * @param filename    Search target
     * @return
     */
    public boolean isExist(NodeRef folderNode, String filename) {
        NodeRef result = fileFolderService.searchSimple(folderNode, filename);
        
        if(result == null) {
            return false;
        }
        
        return fileFolderService.exists(result);
        
    }
    
    /**
     * Prepare nested folder
     */
    public NodeRef prepareNestedFolder(NodeRef rootFolder, String[] paths) {

        // Backup data is always save to Company Home.
        NodeRef currentFolder = rootFolder;

        for(String path : paths) {
            currentFolder = this.prepareFolder(currentFolder, path);
        }

        return currentFolder;
    }
}
