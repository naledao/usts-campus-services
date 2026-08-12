package hhsc.kangnasi.xyz.ustscampusservices.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtil {
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    public static String formatDiff(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        long seconds = Math.abs(duration.getSeconds());

        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "小时";
        } else if (seconds < 2592000) { // 30天近似一个月
            return (seconds / 86400) + "天";
        } else if (seconds < 31104000) { // 12个月近似一年
            return (seconds / 2592000) + "月";
        } else {
            return (seconds / 31104000) + "年";
        }
    }

    /**
     * 计算目标时间与当前时间的间隔（毫秒），范围限定在一天内
     * @param hourStr   小时 (0-23)
     * @param minuteStr 分钟 (0-59)
     * @return 间隔毫秒
     */
    public static long getIntervalMillis(String hourStr, String minuteStr) {
        ZonedDateTime now = nowBusinessZonedDateTime();
        ZonedDateTime target = getNextTriggerTime(hourStr, minuteStr, now);
        return Duration.between(now, target).toMillis();
    }

    public static ZonedDateTime getNextTriggerTime(String hourStr, String minuteStr) {
        return getNextTriggerTime(hourStr, minuteStr, nowBusinessZonedDateTime());
    }

    public static ZonedDateTime getNextTriggerTime(String hourStr, String minuteStr, ZonedDateTime now) {
        int hour = Integer.parseInt(hourStr);
        int minute = Integer.parseInt(minuteStr);

        ZonedDateTime target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        if (!target.isAfter(now)) {
            target = target.plusDays(1);
        }
        return target;
    }

    public static long getNextTriggerEpochMillis(String hourStr, String minuteStr) {
        return getNextTriggerTime(hourStr, minuteStr).toInstant().toEpochMilli();
    }

    public static ZonedDateTime nowBusinessZonedDateTime() {
        return ZonedDateTime.now(BUSINESS_ZONE);
    }

    public static LocalDateTime nowBusinessLocalDateTime() {
        return LocalDateTime.now(BUSINESS_ZONE);
    }

    public static LocalDateTime toBusinessLocalDateTime(Date date) {
        return date.toInstant().atZone(BUSINESS_ZONE).toLocalDateTime();
    }

    /**
     * 获取当前时间的小时和分钟组成的字符串数组
     * @return 包含小时和分钟的字符串数组，格式为[小时, 分钟]，例如["21", "45"]
     */
    public static String[] getCurrentHourMinuteArray() {
        // 获取当前上海业务时间
        LocalTime now = LocalTime.now(BUSINESS_ZONE);
        // 创建时间格式化器，指定格式为24小时制的小时和分钟（HH:mm）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        // 将当前时间按照指定格式转换为字符串，例如 "21:45"
        String timeStr = now.format(formatter);
        // 以冒号为分隔符，将时间字符串拆分为小时和分钟的数组
        return timeStr.split(":"); // 拆成 ["21", "45"]
    }


    public static String getToday() {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return today.format(formatter);
    }
}
