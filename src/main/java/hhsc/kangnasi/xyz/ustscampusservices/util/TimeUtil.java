package hhsc.kangnasi.xyz.ustscampusservices.util;

import java.time.Duration;
import java.time.LocalDateTime;

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
}
