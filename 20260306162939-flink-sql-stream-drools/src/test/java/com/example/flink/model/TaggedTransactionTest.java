package com.example.flink.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaggedTransaction model.
 */
class TaggedTransactionTest {

    @Test
    void testFromTransaction() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-001")
                .accountId("ACC001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .transactionType("DEBIT")
                .transactionTime(Instant.now())
                .tags(Arrays.asList("HIGH_AMOUNT", "HIGH_RISK"))
                .traceId("trace-123")
                .build();

        TaggedTransaction tagged = TaggedTransaction.fromTransaction(transaction);

        assertNotNull(tagged);
        assertEquals("TXN-001", tagged.getTransactionId());
        assertEquals("ACC001", tagged.getAccountId());
        assertEquals("HIGH_AMOUNT,HIGH_RISK", tagged.getTags());
        assertEquals("HIGH_AMOUNT", tagged.getPrimaryTag());
        assertEquals(2, tagged.getTagCount());
        assertNotNull(tagged.getProcessingTime());
    }

    @Test
    void testFromNullTransaction() {
        TaggedTransaction tagged = TaggedTransaction.fromTransaction(null);
        assertNull(tagged);
    }

    @Test
    void testFromTransactionWithEmptyTags() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-001")
                .accountId("ACC001")
                .amount(new BigDecimal("100.00"))
                .tags(new java.util.ArrayList<>())
                .build();

        TaggedTransaction tagged = TaggedTransaction.fromTransaction(transaction);

        assertNotNull(tagged);
        assertEquals("", tagged.getTags());
        assertEquals("UNTAGGED", tagged.getPrimaryTag());
        assertEquals(0, tagged.getTagCount());
    }

    @Test
    void testGetCsvHeader() {
        String[] headers = TaggedTransaction.getCsvHeader();
        assertEquals(18, headers.length);
        assertEquals("transaction_id", headers[0]);
        assertEquals("tags", headers[13]);
        assertEquals("trace_id", headers[17]);
    }

    @Test
    void testToCsvRow() {
        TaggedTransaction tagged = TaggedTransaction.builder()
                .transactionId("TXN-001")
                .accountId("ACC001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .transactionType("DEBIT")
                .tags("HIGH_AMOUNT")
                .primaryTag("HIGH_AMOUNT")
                .tagCount(1)
                .traceId("trace-123")
                .build();

        Object[] row = tagged.toCsvRow();
        assertEquals(18, row.length);
        assertEquals("TXN-001", row[0]);
        assertEquals("100.00", row[2]);
        assertEquals("HIGH_AMOUNT", row[13]);
    }

    @Test
    void testToString() {
        TaggedTransaction tagged = TaggedTransaction.builder()
                .transactionId("TXN-001")
                .amount(new BigDecimal("100.00"))
                .build();

        String str = tagged.toString();
        assertTrue(str.contains("TXN-001"));
        assertTrue(str.contains("100.00"));
    }
}
