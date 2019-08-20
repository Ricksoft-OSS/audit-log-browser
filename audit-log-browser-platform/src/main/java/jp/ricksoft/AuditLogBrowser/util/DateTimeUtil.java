package jp.ricksoft.AuditLogBrowser.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.apache.commons.lang.StringUtils;

public class DateTimeUtil
{

    /**
     * Convert FromDate
     * @param date
     * @param time
     * @return datetime
     */
    public static Long generateFromEpochMilli(String tDate, String tTime) {

        if (StringUtils.isBlank(tDate)) {
            // Alfresco launch since November 2005.
            return DateTimeUtil.convertEpochMilli(LocalDate.of(2005, 11, 1).atStartOfDay());
        } else if (StringUtils.isBlank(tTime)) {
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(tDate).atStartOfDay());
        } else {
            LocalTime time = LocalTime.parse(tTime);
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(tDate).atTime(time));
        }
        
    }

    /**
     * Convert ToDate
     * @param date
     * @param time
     * @return Long
     */
    public static Long generateToEpochMilli(String tDate, String tTime) {
        
        if (StringUtils.isBlank(tDate)) {
            return DateTimeUtil.convertEpochMilli(LocalDateTime.now());
        } else if (StringUtils.isBlank(tTime)) {
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(tDate).plusDays(1).atStartOfDay()) - 1;
        } else {
            LocalTime time = LocalTime.parse(tTime);
            return DateTimeUtil.convertEpochMilli(LocalDate.parse(tDate).atTime(time).plusMinutes(1)) - 1;
        }

    }
    
    public static LocalDateTime generateLocalDateTime(Long EpochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(EpochMilli), ZoneId.systemDefault());
    }

    public static Instant convertToInstant(LocalDateTime target) {
        ZoneId TIMEZONE_DEFAULT = ZoneId.systemDefault();
        return target.toInstant(TIMEZONE_DEFAULT.getRules().getOffset(Instant.EPOCH));
    }

    public static Long convertEpochMilli(LocalDateTime target) {
        return convertToInstant(target).toEpochMilli();
    }
}
