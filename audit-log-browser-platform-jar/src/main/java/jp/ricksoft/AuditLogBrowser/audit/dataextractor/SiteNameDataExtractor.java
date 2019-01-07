/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.AuditLogBrowser.audit.dataextractor;

import java.io.Serializable;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang.StringUtils;

public class SiteNameDataExtractor extends AbstractDataExtractor {

    private SiteService siteService;
    private MessageService messageService;

    private static final String MESSAGE_ID_NO_SITENAME = "ricksoft.auditLogBrowser.log.noSiteName";

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "siteService", this.siteService);
        PropertyCheck.mandatory(this, "messageService", this.messageService);
    }

    /**
     * @return true if this extractor can do anything with the data
     */
    @Override
    public boolean isSupported(Serializable data) {
        return ((data != null) && (data instanceof String || data instanceof NodeRef));
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

        String siteTitle = "";

        if (in instanceof NodeRef) {
            siteTitle = extractSiteNameFromNodeRef((NodeRef)in);
        } else {
            siteTitle = extractSiteNameFromSitePathStr((String)in);
        }

        return siteTitle;
    }

    private String extractSiteNameFromSitePathStr(String sitePath) {
        String siteName = "";

        if (sitePath.contains("st:sites")) {
            siteName = StringUtils.substringBetween(sitePath, "/st:sites/", "/");

            if (logger.isDebugEnabled()) {
                logger.debug("抽出されたサイト名： " + siteName);
            }
        }


        // If content is not in a site, or if it is surf config for user dashboard
        if (StringUtils.isBlank(siteName) || StringUtils.equals(siteName, "cm:surf-config")) {
            // The default site name for content not associated with sites.
            return this.messageService.getMessage(MESSAGE_ID_NO_SITENAME);

        } else {
            String siteShortName = siteName.substring(siteName.indexOf(":")+1);

            SiteInfo siteInfo = siteService.getSite(siteShortName);
            if (logger.isDebugEnabled()) {
                logger.debug("サイト情報： " + siteInfo);
            }
            return siteInfo.getTitle();
        }
    }

    private String extractSiteNameFromNodeRef(NodeRef contentRef) {
        SiteInfo siteInfo = siteService.getSite(contentRef);

        if(siteInfo == null) {
            return this.messageService.getMessage(MESSAGE_ID_NO_SITENAME);
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Extracted Site Name： " + siteInfo.getTitle());
        }
        
        return siteInfo.getTitle();
    }

}
