package hhsc.kangnasi.xyz.ustscampusservices.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
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
        int hour = Integer.parseInt(hourStr);
        int minute = Integer.parseInt(minuteStr);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);

        // 如果目标时间已经过去，则加一天
        if (target.isBefore(now)) {
            target = target.plusDays(1);
        }

        return Duration.between(now, target).toMillis();
    }

    /**
     * 获取当前时间的小时和分钟组成的字符串数组
     * @return 包含小时和分钟的字符串数组，格式为[小时, 分钟]，例如["21", "45"]
     */
    public static String[] getCurrentHourMinuteArray() {
        // 获取当前的本地时间
        LocalTime now = LocalTime.now();
        // 创建时间格式化器，指定格式为24小时制的小时和分钟（HH:mm）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        // 将当前时间按照指定格式转换为字符串，例如 "21:45"
        String timeStr = now.format(formatter);
        // 以冒号为分隔符，将时间字符串拆分为小时和分钟的数组
        return timeStr.split(":"); // 拆成 ["21", "45"]
    }


    public static String getToday() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return today.format(formatter);
    }
}
