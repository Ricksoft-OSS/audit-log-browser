package jp.ricksoft.auditlogbrowser.audit.dataextractor;

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
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class SiteNameDataExtractor extends AbstractDataExtractor {

    private SiteService siteService;
    private MessageService messageService;

    private static final String MSG_ID_NO_SITE = "ricksoft.auditLogBrowser.log.noSiteName";

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
        return ((data instanceof String || data instanceof NodeRef));
    }

    /**
     * Extract the site name / id
     *
     * @param in a string containing the site id
     * @return the site id
     */
    @Override
    public Serializable extractData(Serializable in) {
        
        String siteTitle = "";
        
        if (in instanceof NodeRef) {
            siteTitle = extractSiteNameFromNodeRef((NodeRef)in);
        } else {
            siteTitle = extractSiteNameFromSitePathStr((String)in);
        }
        return siteTitle;
    }
    
    /**
     * @param sitePath
     * @return
     */
    private String extractSiteNameFromSitePathStr(String sitePath) {
        String siteName = "";
        
        if (sitePath.contains("st:sites")) {
            siteName = StringUtils.substringBetween(sitePath, "/st:sites/", "/");
        
            if (logger.isDebugEnabled()) {
                logger.debug("SiteName： " + siteName);
            }
        }
        
        String siteShortName = "";
        if (!StringUtils.isBlank(siteName)) {
            siteShortName = siteName.substring(siteName.indexOf(":")+1);
        }
        
        // If site doesn't exist, return no-site message.
        if (StringUtils.isBlank(siteShortName) || !siteService.hasSite(siteShortName)) {
            // The default site name for content not associated with sites.
            return String.join(": ", this.messageService.getMessage(MSG_ID_NO_SITE), siteShortName);
        
        } else {
        
            SiteInfo siteInfo = siteService.getSite(siteShortName);
            if (logger.isDebugEnabled()) {
                logger.debug("SiteInfo： " + siteInfo);
            }
            
            return siteInfo.getTitle();
        }
    
    }
    
    /**
     * 
     * @param contentRef
     * @return
     */
    private String extractSiteNameFromNodeRef(NodeRef contentRef) {
        SiteInfo siteInfo = siteService.getSite(contentRef);
        
        if(siteInfo == null) {
            return String.join(": ", this.messageService.getMessage(MSG_ID_NO_SITE), contentRef.toString());
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("SiteInfo： " + siteInfo.getTitle());
        }
            
        return siteInfo.getTitle();
    }

}
