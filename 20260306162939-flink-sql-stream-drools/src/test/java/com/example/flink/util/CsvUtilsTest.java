package com.example.flink.util;

import com.example.flink.model.TaggedTransaction;
import com.example.flink.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CsvUtils.
 */
class CsvUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void testGenerateSampleTransactions() {
        List<Transaction> transactions = CsvUtils.generateSampleTransactions(10);
        assertEquals(10, transactions.size());
        
        Transaction first = transactions.get(0);
        assertNotNull(first.getTransactionId());
        assertNotNull(first.getAccountId());
        assertNotNull(first.getAmount());
        assertNotNull(first.getTransactionType());
    }

    @Test
    void testWriteAndReadTransactions() throws Exception {
        // Generate sample data
        String filePath = tempDir.resolve("test-transactions.csv").toString();
        CsvUtils.writeSampleCsv(filePath, 5);

        // Verify file exists
        assertTrue(Files.exists(Path.of(filePath)));

        // Read transactions
        List<Transaction> transactions = CsvUtils.readTransactions(filePath);
        assertEquals(5, transactions.size());

        // Verify first transaction
        Transaction first = transactions.get(0);
        assertNotNull(first.getTransactionId());
        assertNotNull(first.getAccountId());
        assertTrue(first.isValid());
    }

    @Test
    void testWriteTaggedTransactions() throws Exception {
        // Create sample tagged transactions
        List<TaggedTransaction> taggedTransactions = List.of(
                TaggedTransaction.builder()
                        .transactionId("TXN-001")
                        .accountId("ACC001")
                        .tags("HIGH_AMOUNT,HIGH_RISK")
                        .primaryTag("HIGH_RISK")
                        .tagCount(2)
                        .build(),
                TaggedTransaction.builder()
                        .transactionId("TXN-002")
                        .accountId("ACC002")
                        .tags("TRANSFER")
                        .primaryTag("TRANSFER")
                        .tagCount(1)
                        .build()
        );

        String filePath = tempDir.resolve("test-tagged.csv").toString();
        CsvUtils.writeTaggedTransactions(filePath, taggedTransactions);

        // Verify file exists and has content
        assertTrue(Files.exists(Path.of(filePath)));
        List<String> lines = Files.readAllLines(Path.of(filePath));
        assertTrue(lines.size() > 2); // Header + 2 data rows
        assertTrue(lines.get(0).contains("transaction_id"));
    }

    @Test
    void testReadTransactionsFileNotFound() {
        assertThrows(Exception.class, () -> {
            CsvUtils.readTransactions("/nonexistent/path/file.csv");
        });
    }

    @Test
    void testWriteSampleCsv() throws Exception {
        String filePath = tempDir.resolve("sample.csv").toString();
        CsvUtils.writeSampleCsv(filePath, 20);

        assertTrue(Files.exists(Path.of(filePath)));
        
        List<String> lines = Files.readAllLines(Path.of(filePath));
        assertEquals(21, lines.size()); // Header + 20 data rows
    }
}
