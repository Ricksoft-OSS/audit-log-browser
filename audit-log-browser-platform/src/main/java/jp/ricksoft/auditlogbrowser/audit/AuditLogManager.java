/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
package jp.ricksoft.auditlogbrowser.audit;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;

import jp.ricksoft.auditlogbrowser.util.DateTimeUtil;

public class AuditLogManager
{

    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";

    private String appName;
    private AuditService auditService;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    public List<Map<String, Object>> getAuditLogs(Long fromTime, Long toTime, Long fromId, String user) throws IllegalArgumentException
    {
        return this.getAuditLogs(fromTime, toTime, fromId, user, 100);
    }

    /**
     * Get Audit Logs
     * 
     * @author ebihara.yuki
     */
    public List<Map<String, Object>> getAuditLogs(Long fromTime, Long toTime, Long fromId, String user, int maxUnit) throws IllegalArgumentException
    {

        // Audit log query callback function setting.
        MyAuditQueryCallback callback = new MyAuditQueryCallback();

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
        auditService.auditQuery(callback, params, maxUnit);

        return callback.getEntries();
    }

    public LocalDateTime getOldestLoggedDateTime(){
        Map<String, Object> entry = this.getAuditLogs(null, null, null, null, 1).get(0);
        return (LocalDateTime) entry.get(KEY_TIME);
    }

    /**
     * Delete Audit Logs（Need to set period）
     *
     * @param fromTime  FromDate to EpochMilli
     * @param toTime    ToDate to EpochMilli
     */
    public void delete(Long fromTime, Long toTime)
    {
        auditService.clearAudit(appName, fromTime, toTime);
    }

    private class MyAuditQueryCallback implements AuditQueryCallback
    {
        // Query result save
        private List<Map<String, Object>> entries = new ArrayList<>();

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
            Map<String, Object> entry = new HashMap<>();
            entry.put(KEY_ID, entryId);
            entry.put(KEY_TIME, DateTimeUtil.convertLocalDateTime(time));

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
