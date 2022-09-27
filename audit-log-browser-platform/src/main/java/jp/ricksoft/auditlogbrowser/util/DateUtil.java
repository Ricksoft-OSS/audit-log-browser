package jp.ricksoft.auditlogbrowser.util;

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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtil {
    /**
     * Convert FromDate
     * @param date
     * @param time
     * @return datetime
     */
    public static long generateFromEpochMilli(LocalDate tDate) {

        if (tDate == null) {
            // Alfresco launch since November 2005.
            return convertEpochMilli(LocalDate.of(2005, 11, 1).atStartOfDay());
        } else {
            return convertEpochMilli(tDate.atStartOfDay());
        }
        
    }

    /**
     * Convert ToDate
     * @param date
     * @param time
     * @return long
     */
    public static long generateToEpochMilli(LocalDate tDate) {
        
        if (tDate == null) {
            return convertEpochMilli(LocalDateTime.now());
        } else {
            return convertEpochMilli(tDate.plusDays(1).atStartOfDay()) - 1;
        }

    }

    private static Instant localDateTimeToInstant(LocalDateTime target) {
        ZoneId TIMEZONE_DEFAULT = ZoneId.systemDefault();
        return target.toInstant(TIMEZONE_DEFAULT.getRules().getOffset(Instant.EPOCH));
    }

    private static long convertEpochMilli(LocalDateTime target) {
        return localDateTimeToInstant(target).toEpochMilli();
    }
}
