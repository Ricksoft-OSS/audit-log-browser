/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.audit_log_browser.audit;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;

import jp.ricksoft.audit_log_browser.util.DateTimeUtil;

public class AuditLogManager
{

    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";

    private AuditService auditService;
    private static final DateTimeFormatter FORMAT_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    /**
     * Get Audit Logs
     * 
     * @author ebihara.yuki
     * @param appName  Audit Application Name
     * @param maxItems  Max Size per Query
     */
    public List<Map<String, Object>> getAuditLogs(String appName, int maxItems, Long fromTime, Long toTime, Long fromId,
            String user) throws IllegalArgumentException
    {

        // Audit log query callback function setting.
        AuditQueryCallback callback = new MyAuditQueryCallback();

        // Query Condition Setting
        AuditQueryParameters params = new AuditQueryParameters();

        params.setApplicationName(appName);
        if (fromId != null)
        {
            params.setFromId(fromId);
        }
        if (fromTime != null)
        {
            params.setFromTime(fromTime);
        }
        if (toTime != null)
        {
            params.setToTime(toTime);
        }
        if (user != null && !user.isEmpty())
        {
            params.setUser(user);
        }
        params.setForward(true);

        // Execute Query
        auditService.auditQuery(callback, params, maxItems);

        return ((MyAuditQueryCallback) callback).getEntries();
    }

    /**
     * Delete Audit Logs（Need to set period）
     * 
     * @param appName   Audit Application Name
     * @param fromTime  FromDate to EpochMilli
     * @param toTime    ToDate to EpochMilli
     */
    public void delete(String appName, Long fromTime, Long toTime)
    {
        auditService.clearAudit(appName, fromTime, toTime);
    }

    private class MyAuditQueryCallback implements AuditQueryCallback
    {
        // Query result save
        private List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();

        public List<Map<String, Object>> getEntries()
        {
            return entries;
        }

        /**
         * Determines whether the value argument needs to be set when the handleAuditEntry method is called from this callback.
         * 
         * @return Need to set 'values' argument, set true.
         */
        @Override
        public boolean valuesRequired()
        {
            return true;
        }

        /**
         * Process audit entry error
         * 
         * @param entryId  Audit entry ID
         * @param errorMsg  Error Message
         * @param error    Exception cause of error
         * @return if continue, return true. if not, false.
         */
        @Override
        public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
        {
            return true;
        }

        /**
         * Process audit log entry
         * 
         * @param entryId  Audit entry ID
         * @param appName  Audit app name
         * @param user  Audit action user
         * @param time  Audit time
         * @param values  other audit value
         * @return if continue, return true. if not, false.
         */
        @Override
        public boolean handleAuditEntry(Long entryId, String appName, String user, long time,
                Map<String, Serializable> values)
        {
            Map<String, Object> entry = new HashMap<String, Object>();
            entry.put(KEY_ID, entryId);
            entry.put(KEY_TIME, DateTimeUtil.generateLocalDateTime(time)
                    .format(FORMAT_DATETIME.withResolverStyle(ResolverStyle.STRICT)));

            if (values != null)
            {
                values.forEach((key, value) ->
                {
                    entry.put(key.substring(key.lastIndexOf("/") + 1), value);
                });
            }

            entries.add(entry);
            return true;
        }
    }
}
