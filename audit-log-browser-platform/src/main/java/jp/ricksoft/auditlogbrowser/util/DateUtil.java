package jp.ricksoft.auditlogbrowser.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtil
{
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
