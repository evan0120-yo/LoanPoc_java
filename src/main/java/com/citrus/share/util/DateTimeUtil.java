package com.citrus.share.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期時間工具類
 * 印度時區: Asia/Kolkata (UTC+5:30)
 */
public final class DateTimeUtil {

    public static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
    }

    /**
     * 取得印度當前時間
     */
    public static LocalDateTime nowIndia() {
        return LocalDateTime.now(INDIA_ZONE);
    }

    /**
     * 取得印度當前日期
     */
    public static LocalDate todayIndia() {
        return LocalDate.now(INDIA_ZONE);
    }

    /**
     * Instant 轉印度時間
     */
    public static LocalDateTime toIndiaTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, INDIA_ZONE);
    }

    /**
     * 印度時間轉 Instant
     */
    public static Instant toInstant(LocalDateTime indiaTime) {
        return indiaTime.atZone(INDIA_ZONE).toInstant();
    }

    /**
     * 計算 DPD (Days Past Due)
     */
    public static int calculateDpd(LocalDate dueDate) {
        LocalDate today = todayIndia();
        if (today.isAfter(dueDate)) {
            return (int) (today.toEpochDay() - dueDate.toEpochDay());
        }
        return 0;
    }
}
