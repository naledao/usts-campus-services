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
}
