package jp.ricksoft.auditlogbrowser.audit;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.ricksoft.auditlogbrowser.util.DateTimeUtil;

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

public class AuditLogManager {

    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogManager.class);

    private String appName;
    private AuditService auditService;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    public List<Map<String, Object>> getAuditLogs(Long fromTime, Long toTime, Long fromId,
            String user, Map<String, Serializable> searchValues) {
        return this.getAuditLogs(fromTime, toTime, fromId, user, searchValues, 100);
    }

    /**
     * Get Audit Logs
     *
     * @author ebihara.yuki
     */
    public List<Map<String, Object>> getAuditLogs(Long fromTime, Long toTime, Long fromId,
            String user, Map<String, Serializable> searchValues, int maxUnit) {

        // Audit log query callback function setting.
        MyAuditQueryCallback callback = new MyAuditQueryCallback();

        // Execute Query
        auditService.auditQuery(callback,
                this.buildAuditQueryParameters(fromTime, toTime, fromId, user, searchValues), maxUnit);

        return callback.getEntries();
    }

    public LocalDateTime getOldestLoggedDateTime() {
        LocalDateTime oldestLocalDateTime = LocalDateTime.now();

        List<Map<String, Object>> entries = this.getAuditLogs(null, null, null, null, null, 1);
        if (entries != null && !entries.isEmpty()) {
            oldestLocalDateTime = (LocalDateTime) entries.get(0).get(KEY_TIME);
        }
        return oldestLocalDateTime;
    }

    /**
     * Delete Audit Logs（Need to set period）
     *
     * @param fromTime FromDate to EpochMilli
     * @param toTime   ToDate to EpochMilli
     */
    public void delete(Long fromTime, Long toTime) {
        auditService.clearAudit(appName, fromTime, toTime);
    }

    /**
     * Get the total number of audit logs
     */
    public int getTotalAuditEntriesNum(Long fromTime, Long toTime, String user,
            Map<String, Serializable> searchValues) {
        return this.auditService.getAuditEntriesCountByAppAndProperties(this.appName,
                this.buildAuditQueryParameters(fromTime, toTime, null, user, searchValues));
    }

    private AuditQueryParameters buildAuditQueryParameters(Long fromTime, Long toTime, Long fromId,
            String user, Map<String, Serializable> searchValues) {
        // Query Condition Setting
        AuditQueryParameters params = new AuditQueryParameters();

        params.setApplicationName(appName);
        if (fromId != null) {
            params.setFromId(fromId);
        }
        if (fromTime != null) {
            params.setFromTime(fromTime);
        }
        if (toTime != null) {
            params.setToTime(toTime);
        }
        if (user != null && !user.isEmpty()) {
            params.setUser(user);
        }
        if (searchValues != null && !searchValues.isEmpty()) {
            searchValues.forEach((k, v) -> {
                params.addSearchKey(k, v);
            });
        }

        LOG.trace("{} / {} / {} / {} / {}", params.getFromId(), params.getFromTime(), params.getToTime(),
                params.getUser(), params.getSearchKeyValues());

        params.setForward(true);

        return params;
    }

    private class MyAuditQueryCallback implements AuditQueryCallback {
        // Query result save
        private List<Map<String, Object>> entries = new ArrayList<>();

        public List<Map<String, Object>> getEntries() {
            return entries;
        }

        /**
         * Determines whether the value argument needs to be set when the
         * handleAuditEntry method is
         * called from this callback.
         *
         * @return Need to set 'values' argument, set true.
         */
        @Override
        public boolean valuesRequired() {
            return true;
        }

        /**
         * Process audit entry error
         *
         * @param entryId  Audit entry ID
         * @param errorMsg Error Message
         * @param error    Exception cause of error
         * @return if continue, return true. if not, false.
         */
        @Override
        public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {
            return true;
        }

        /**
         * Process audit log entry
         *
         * @param entryId Audit entry ID
         * @param appName Audit app name
         * @param user    Audit action user
         * @param time    Audit time
         * @param values  other audit value
         * @return if continue, return true. if not, false.
         */
        @Override
        public boolean handleAuditEntry(Long entryId, String appName, String user, long time,
                Map<String, Serializable> values) {
            Map<String, Object> entry = new HashMap<>();
            entry.put(KEY_ID, entryId);
            entry.put(KEY_TIME, DateTimeUtil.convertLocalDateTime(time));

            if (values != null) {
                values.forEach(
                        (key, value) -> entry.put(key.substring(key.lastIndexOf("/") + 1), value));
            }

            entries.add(entry);
            return true;
        }
    }
}
