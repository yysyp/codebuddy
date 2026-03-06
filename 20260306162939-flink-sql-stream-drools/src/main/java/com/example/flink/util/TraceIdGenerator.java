package com.example.flink.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for generating trace IDs for distributed tracing.
 * Thread-safe implementation with multiple ID generation strategies.
 */
public final class TraceIdGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final String PREFIX = "TXN";

    private TraceIdGenerator() {
        // Utility class - prevent instantiation
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Generate a new trace ID using UUID
     *
     * @return unique trace ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate a short trace ID with prefix and sequence
     *
     * @return short unique ID
     */
    public static String generateShortTraceId() {
        return PREFIX + System.currentTimeMillis() + "-" + SEQUENCE.incrementAndGet();
    }

    /**
     * Generate a transaction ID with specific format
     *
     * @param accountId the account ID
     * @return formatted transaction ID
     */
    public static String generateTransactionId(String accountId) {
        String prefix = (accountId != null && !accountId.isEmpty())
                ? accountId.substring(0, Math.min(4, accountId.length()))
                : "UNKN";
        return prefix + "-" + System.currentTimeMillis() + "-" + SEQUENCE.incrementAndGet();
    }

    /**
     * Generate trace ID with component prefix for Flink subtasks
     *
     * @param component component name
     * @param subtaskIndex subtask index
     * @return trace ID with component info
     */
    public static String generateComponentTraceId(String component, int subtaskIndex) {
        return component + "-" + subtaskIndex + "-" + generateTraceId();
    }
}
