package com.example.taskscheduler.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String formatDateTime(LocalDateTime dt) {
        return dt.format(FMT);
    }

    public static LocalDateTime parseDateTime(String s) {
        try {
            return LocalDateTime.parse(s, FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
