package com.example.flink.transaction.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility class for Trace ID management
 * Provides distributed tracing capabilities for observability
 */
public class TraceIdUtil {

    private static final String TRACE_ID_KEY = "traceId";

    /**
     * Generate a new unique trace ID
     *
     * @return UUID-based trace ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Set trace ID in MDC (Mapped Diagnostic Context)
     * This ensures trace ID is available in all log statements
     *
     * @param traceId the trace ID to set
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }

    /**
     * Get current trace ID from MDC
     *
     * @return the current trace ID, or null if not set
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * Remove trace ID from MDC
     * Should be called at the end of request processing
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * Execute a runnable with trace ID context
     *
     * @param traceId the trace ID to use
     * @param runnable the task to execute
     */
    public static void executeWithTraceId(String traceId, Runnable runnable) {
        try {
            setTraceId(traceId);
            runnable.run();
        } finally {
            clearTraceId();
        }
    }
}
