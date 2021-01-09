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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class ScheduledJob extends AbstractScheduledLockedJob {

    private static final String JOB_SCHEDULER = "jobScheduler";
    private static final String IS_ENABLED = "isEnabled";
    private static final String MSG_NO_SCHEDULER = "No valid job scheduler.";
    private static final String MSG_SCHEDULE_DISABLED = "Scheduler is disabled.";
    
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledJob.class);

    @Override
    public void executeJob(JobExecutionContext jobContext) {
        JobDataMap jobData = jobContext.getJobDetail().getJobDataMap();

        // Job executer and setting params.
        Object executerObj = jobData.get(JOB_SCHEDULER);
        boolean isEnabled  = Boolean.parseBoolean((String) jobData.get(IS_ENABLED));
        
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
            jobScheduler.execute();
            return null;
        }, AuthenticationUtil.getSystemUserName());
    }

}
