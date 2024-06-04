package com.diaoling.utils.misc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author DiaoLing
 * @since 3/12/2024
 */
public class Logger {
    private static final String INFO_PREFIX = "*";
    private static final String ERROR_PREFIX = "!";
    private static final String DEBUG_PREFIX = "/";
    private static final String WARNING_PREFIX = "-";
    private static final String NOTICE_PREFIX = "+";
    private static final String SPECIAL_PREFIX = "~";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static void log(String prefix, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String formattedMessage = String.format("[%s] [%s] %s", timestamp, prefix, message);
        if (prefix.equals(ERROR_PREFIX)) {
            System.err.println(formattedMessage);
        } else {
            System.out.println(formattedMessage);
        }
    }

    public static void info(String message) {
        log(INFO_PREFIX, message);
    }

    public static void error(String message) {
        log(ERROR_PREFIX, message);
    }

    public static void debug(String message) {
        log(DEBUG_PREFIX, message);
    }

    public static void warning(String message) {
        log(WARNING_PREFIX, message);
    }

    public static void notice(String message) {
        log(NOTICE_PREFIX, message);
    }

    public static void special(String message) {
        log(SPECIAL_PREFIX, message);
    }
}
