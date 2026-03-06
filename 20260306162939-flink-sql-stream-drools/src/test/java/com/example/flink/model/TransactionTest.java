package com.example.flink.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Transaction model.
 */
class TransactionTest {

    @Test
    void testTransactionBuilder() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-001")
                .accountId("ACC001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .transactionType("DEBIT")
                .transactionTime(Instant.now())
                .build();

        assertNotNull(transaction);
        assertEquals("TXN-001", transaction.getTransactionId());
        assertEquals("ACC001", transaction.getAccountId());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
    }

    @Test
    void testAddTag() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN-001");
        transaction.setTags(new java.util.ArrayList<>());

        transaction.addTag("HIGH_AMOUNT");
        assertTrue(transaction.hasTag("HIGH_AMOUNT"));
        assertEquals(1, transaction.getTags().size());

        // Adding same tag again should not duplicate
        transaction.addTag("HIGH_AMOUNT");
        assertEquals(1, transaction.getTags().size());
    }

    @Test
    void testGetTagsAsString() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-001")
                .tags(Arrays.asList("TAG1", "TAG2", "TAG3"))
                .build();

        assertEquals("TAG1,TAG2,TAG3", transaction.getTagsAsString());
    }

    @Test
    void testIsValid() {
        Transaction validTransaction = Transaction.builder()
                .transactionId("TXN-001")
                .accountId("ACC001")
                .amount(new BigDecimal("100.00"))
                .transactionType("DEBIT")
                .transactionTime(Instant.now())
                .build();

        assertTrue(validTransaction.isValid());

        Transaction invalidTransaction = new Transaction();
        assertFalse(invalidTransaction.isValid());
    }

    @Test
    void testInitialize() {
        Transaction transaction = new Transaction();
        assertNull(transaction.getTransactionId());
        assertNull(transaction.getTraceId());

        transaction.initialize();

        assertNotNull(transaction.getTransactionId());
        assertNotNull(transaction.getTraceId());
        assertNotNull(transaction.getTransactionTime());
        assertNotNull(transaction.getTags());
    }

    @Test
    void testEqualsAndHashCode() {
        Transaction tx1 = Transaction.builder()
                .transactionId("TXN-001")
                .accountId("ACC001")
                .build();

        Transaction tx2 = Transaction.builder()
                .transactionId("TXN-001")
                .accountId("ACC002")
                .build();

        Transaction tx3 = Transaction.builder()
                .transactionId("TXN-002")
                .accountId("ACC001")
                .build();

        assertEquals(tx1, tx2); // Same transaction ID
        assertNotEquals(tx1, tx3); // Different transaction ID
        assertEquals(tx1.hashCode(), tx2.hashCode());
    }
}
