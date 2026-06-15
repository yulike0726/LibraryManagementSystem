package com.library.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具类
 * 处理借阅日期、到期日、超期天数等计算
 */
public class DateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 获取当前日期字符串 */
    public static String today() {
        return LocalDate.now().format(FORMATTER);
    }

    /** 计算从某天起N天后的日期 */
    public static String addDays(String dateStr, int days) {
        LocalDate date = LocalDate.parse(dateStr, FORMATTER);
        return date.plusDays(days).format(FORMATTER);
    }

    /** 计算两个日期之间的天数差（date2 - date1） */
    public static long daysBetween(String dateStr1, String dateStr2) {
        LocalDate date1 = LocalDate.parse(dateStr1, FORMATTER);
        LocalDate date2 = LocalDate.parse(dateStr2, FORMATTER);
        return ChronoUnit.DAYS.between(date1, date2);
    }

    /** 判断date1是否在date2之后（date1 > date2） */
    public static boolean isAfter(String dateStr1, String dateStr2) {
        return daysBetween(dateStr2, dateStr1) > 0;
    }

    /** 判断当前日期是否已超过指定日期 */
    public static boolean isOverdue(String dueDate) {
        return isAfter(today(), dueDate);
    }

    /** 获取超期天数（0表示未超期） */
    public static long getOverdueDays(String dueDate) {
        long diff = daysBetween(dueDate, today());
        return Math.max(0, diff);
    }

    /** 格式化日期验证 */
    public static boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
