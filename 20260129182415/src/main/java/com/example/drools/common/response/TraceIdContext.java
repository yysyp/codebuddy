package com.example.drools.common.response;

import java.util.UUID;

/**
 * Thread-local context for storing traceId
 */
public class TraceIdContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    /**
     * Get current traceId
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
     * Set traceId
     */
    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    /**
     * Clear traceId
     */
    public static void clearTraceId() {
        TRACE_ID.remove();
    }

    /**
     * Generate new traceId
     */
    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
