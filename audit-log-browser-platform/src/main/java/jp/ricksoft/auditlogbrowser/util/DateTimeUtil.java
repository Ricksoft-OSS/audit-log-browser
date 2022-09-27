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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class DateTimeUtil {
    public static LocalDateTime convertLocalDateTime(long EpochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(EpochMilli), ZoneId.systemDefault());
    }

    public static Instant localDateTimeToInstant(LocalDateTime target) {
        ZoneOffset DEFAULT_OFFSET = ZoneId.systemDefault().getRules().getOffset(Instant.EPOCH);
        return target.toInstant(DEFAULT_OFFSET);
    }

    public static long convertEpochMilli(LocalDateTime target) {
        return localDateTimeToInstant(target).toEpochMilli();
    }

    public static String convertEpochMilliToYYYYMMDD(long datetimeEpochMilli) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd").withResolverStyle(ResolverStyle.STRICT);
        LocalDate targetDate = LocalDate.ofInstant(Instant.ofEpochMilli(datetimeEpochMilli), ZoneId.systemDefault());
        return targetDate.format(dateFormat);
    }

    public static long getEndOfDateEpochMilli(long dateEpochMilli) {
        LocalDateTime nextDate = convertLocalDateTime(dateEpochMilli).toLocalDate().plusDays(1).atStartOfDay();
        return convertEpochMilli(nextDate) -1;
    }

}
