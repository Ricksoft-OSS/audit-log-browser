/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.schedule;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledJob extends AbstractScheduledLockedJob implements StatefulJob {

    private static final String JOB_SCHEDULER = "jobScheduler";
    private static final String IS_ENABLED = "isEnabled";
    private static final String DO_DELETE  = "doDelete";
    private static final String STORAGE_PERIOD = "storagePeriod";
    private static final String ID_PROP_FOLDERNAME = "backupDirectory";
    private static final String MSG_NO_SCHEDULER = "No valid job scheduler.";
    private static final String MSG_SCHEDULE_DISABLED = "Scheduler is disabled.";
    
    private static final Logger LOG = LoggerFactory.getLogger(AuditlogArchiveScheduler.class);

    @Override
    public void executeJob(JobExecutionContext jobContext) {
        JobDataMap jobData = jobContext.getJobDetail().getJobDataMap();

        // Job executer and setting params.
        Object executerObj = jobData.get(JOB_SCHEDULER);
        boolean isEnabled  = Boolean.parseBoolean((String) jobData.get(IS_ENABLED));
        boolean doDelete   = Boolean.parseBoolean((String) jobData.get(DO_DELETE));
        int storagePeriod  = Integer.parseInt((String) jobData.get(STORAGE_PERIOD));
        final String backupFolderPath = (String) jobData.get(ID_PROP_FOLDERNAME);
        
        if (!isEnabled) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MSG_SCHEDULE_DISABLED);
            }
            return;
        }
        
        // No valid job scheduler.
        if (!(executerObj instanceof AuditlogArchiveScheduler)) {
            throw new AlfrescoRuntimeException(MSG_NO_SCHEDULER);
        }

        final AuditlogArchiveScheduler jobScheduler = (AuditlogArchiveScheduler) executerObj;

        // Run schedule job as 
        AuthenticationUtil.runAs(() -> {
            jobScheduler.execute(storagePeriod, doDelete, backupFolderPath);
            return null;
        }, AuthenticationUtil.getSystemUserName());
    }

}
