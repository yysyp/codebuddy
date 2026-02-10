package com.example.flink.transaction.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for time operations
 * All time operations use UTC Instant as the base type
 */
public class TimeUtil {

    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.of("UTC"));

    /**
     * Convert Instant to formatted UTC string
     *
     * @param instant the instant to format
     * @return formatted UTC string
     */
    public static String formatUtc(Instant instant) {
        if (instant == null) {
            return null;
        }
        return UTC_FORMATTER.format(instant);
    }

    /**
     * Parse UTC string to Instant
     *
     * @param utcString the UTC string to parse
     * @return Instant object
     */
    public static Instant parseUtc(String utcString) {
        if (utcString == null || utcString.isEmpty()) {
            return null;
        }
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(utcString, UTC_FORMATTER);
            return zdt.toInstant();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid UTC format: " + utcString, e);
        }
    }

    /**
     * Get current UTC timestamp
     *
     * @return current Instant in UTC
     */
    public static Instant nowUtc() {
        return Instant.now();
    }

    /**
     * Calculate duration between two instants in milliseconds
     *
     * @param start the start instant
     * @param end the end instant
     * @return duration in milliseconds
     */
    public static long durationMillis(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.Duration.between(start, end).toMillis();
    }
}
