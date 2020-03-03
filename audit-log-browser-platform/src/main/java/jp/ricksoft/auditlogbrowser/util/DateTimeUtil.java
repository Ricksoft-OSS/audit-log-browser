package jp.ricksoft.auditlogbrowser.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class DateTimeUtil
{
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
