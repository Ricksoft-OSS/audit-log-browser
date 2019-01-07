/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.AuditLogBrowser.NodeRef;

import java.io.File;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;


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
