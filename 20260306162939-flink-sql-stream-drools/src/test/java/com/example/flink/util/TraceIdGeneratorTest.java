package com.example.flink.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TraceIdGenerator.
 */
class TraceIdGeneratorTest {

    @Test
    void testGenerateTraceId() {
        String traceId1 = TraceIdGenerator.generateTraceId();
        String traceId2 = TraceIdGenerator.generateTraceId();

        assertNotNull(traceId1);
        assertNotNull(traceId2);
        assertNotEquals(traceId1, traceId2);
        assertFalse(traceId1.contains("-")); // Should not contain hyphens
    }

    @Test
    void testGenerateShortTraceId() {
        String shortId1 = TraceIdGenerator.generateShortTraceId();
        String shortId2 = TraceIdGenerator.generateShortTraceId();

        assertNotNull(shortId1);
        assertNotNull(shortId2);
        assertTrue(shortId1.startsWith("TXN"));
        assertNotEquals(shortId1, shortId2);
    }

    @Test
    void testGenerateTransactionId() {
        String txId1 = TraceIdGenerator.generateTransactionId("ACC001");
        String txId2 = TraceIdGenerator.generateTransactionId("ACC001");

        assertNotNull(txId1);
        assertNotNull(txId2);
        assertTrue(txId1.startsWith("ACC0"));
        assertNotEquals(txId1, txId2);
    }

    @Test
    void testGenerateTransactionIdWithNullAccount() {
        String txId = TraceIdGenerator.generateTransactionId(null);
        assertNotNull(txId);
        assertTrue(txId.startsWith("UNKN"));
    }

    @Test
    void testGenerateComponentTraceId() {
        String componentTraceId = TraceIdGenerator.generateComponentTraceId("FlinkJob", 1);
        assertNotNull(componentTraceId);
        assertTrue(componentTraceId.startsWith("FlinkJob-1-"));
    }

    @Test
    void testUniqueness() {
        // Generate many IDs and verify uniqueness
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(TraceIdGenerator.generateTraceId());
        }
        assertEquals(1000, ids.size());
    }
}
