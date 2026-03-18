package com.transaction.tagging.common.util;

import java.util.UUID;

/**
 * Utility class for generating and managing trace IDs.
 * Used for distributed tracing across services.
 */
public final class TraceContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceContext() {
        // Utility class
    }

    /**
     * Generate a new trace ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Set the trace ID for the current thread
     */
    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    /**
     * Get the trace ID for the current thread
     */
    public static String getTraceId() {
        String traceId = TRACE_ID.get();
        if (traceId == null) {
            traceId = generateTraceId();
            TRACE_ID.set(traceId);
        }
        return traceId;
    }

    /**
     * Clear the trace ID for the current thread
     */
    public static void clear() {
        TRACE_ID.remove();
    }

    /**
     * Get or generate a trace ID
     */
    public static String getOrCreateTraceId() {
        return getTraceId();
    }
}
