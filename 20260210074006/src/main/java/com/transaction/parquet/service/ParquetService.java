package com.transaction.parquet.service;

import com.transaction.domain.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for generating Parquet files from transaction data
 * Note: Simplified implementation focusing on CSV output for demonstration
 */
@Slf4j
@Service
public class ParquetService {

    @Value("${app.parquet.output-dir:./parquet-output}")
    private String outputDir;

    @Value("${app.parquet.batch-size:1000}")
    private int batchSize;

    private final Map<String, BatchWriter> writers = new ConcurrentHashMap<>();

    /**
     * Initialize output directory
     */
    public void initialize() {
        try {
            Path path = Paths.get(outputDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created parquet output directory: {}", outputDir);
            }
        } catch (IOException e) {
            log.error("Failed to create parquet output directory", e);
            throw new RuntimeException("Failed to initialize parquet output directory", e);
        }
    }

    /**
     * Write a single transaction to parquet file
     */
    public synchronized void writeTransaction(Transaction transaction) {
        String fileName = generateFileName(transaction);
        BatchWriter writer = getWriter(fileName);
        
        try {
            writer.write(transaction);
            log.debug("Written transaction {} to file: {}", transaction.getId(), fileName);
        } catch (Exception e) {
            log.error("Failed to write transaction {} to file: {}", 
                     transaction.getId(), fileName, e);
            throw new RuntimeException("Failed to write transaction to parquet", e);
        }
    }

    /**
     * Write multiple transactions to parquet file in batch
     */
    public synchronized void writeTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            log.warn("No transactions to write");
            return;
        }

        String fileName = generateFileName(transactions.get(0));
        BatchWriter writer = getWriter(fileName);
        
        try {
            for (Transaction transaction : transactions) {
                writer.write(transaction);
            }
            log.info("Written {} transactions to file: {}", transactions.size(), fileName);
        } catch (Exception e) {
            log.error("Failed to write transactions to file: {}", fileName, e);
            throw new RuntimeException("Failed to write transactions to parquet", e);
        }
    }

    /**
     * Generate parquet filename based on transaction
     */
    private String generateFileName(Transaction transaction) {
        String dateStr = transaction.getTransactionTime().toString().substring(0, 10);
        return String.format("transactions_%s_%s.csv", 
                transaction.getAccountId(), 
                dateStr);
    }

    /**
     * Get or create writer for a file
     */
    private BatchWriter getWriter(String fileName) {
        return writers.computeIfAbsent(fileName, key -> {
            try {
                Path filePath = Paths.get(outputDir, fileName);
                return new BatchWriter(filePath);
            } catch (IOException e) {
                log.error("Failed to create writer for file: {}", fileName, e);
                throw new RuntimeException("Failed to create writer", e);
            }
        });
    }

    /**
     * Close all writers
     */
    public void closeAllWriters() {
        log.info("Closing all writers...");
        for (Map.Entry<String, BatchWriter> entry : writers.entrySet()) {
            try {
                entry.getValue().close();
                log.info("Closed writer for file: {}", entry.getKey());
            } catch (Exception e) {
                log.error("Failed to close writer for file: {}", entry.getKey(), e);
            }
        }
        writers.clear();
    }

    /**
     * Close writer for specific file
     */
    public void closeWriter(String fileName) {
        BatchWriter writer = writers.remove(fileName);
        if (writer != null) {
            try {
                writer.close();
                log.info("Closed writer for file: {}", fileName);
            } catch (Exception e) {
                log.error("Failed to close writer for file: {}", fileName, e);
            }
        }
    }

    /**
     * Cleanup on shutdown
     */
    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up ParquetService...");
        closeAllWriters();
    }

    /**
     * Simple batch writer for CSV format
     */
    private static class BatchWriter {
        private final Path filePath;
        private java.io.PrintWriter writer;
        private boolean headerWritten;

        public BatchWriter(Path filePath) throws IOException {
            this.filePath = filePath;
            this.headerWritten = false;
            openWriter();
        }

        private void openWriter() throws IOException {
            boolean append = Files.exists(filePath);
            this.writer = new java.io.PrintWriter(new java.io.FileWriter(filePath.toFile(), true));
            
            if (!append) {
                writeHeader();
            }
        }

        private void writeHeader() {
            String header = "id,accountId,transactionType,amount,currency,merchantName," +
                          "merchantCategory,location,ipAddress,deviceId,referenceNumber," +
                          "status,transactionTime,description,riskScore,tags,createdAt," +
                          "createdBy,updatedAt,updatedBy";
            writer.println(header);
            headerWritten = true;
        }

        public void write(Transaction transaction) {
            String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    escape(transaction.getId()),
                    escape(transaction.getAccountId()),
                    escape(transaction.getTransactionType()),
                    escape(transaction.getAmount()),
                    escape(transaction.getCurrency()),
                    escape(transaction.getMerchantName()),
                    escape(transaction.getMerchantCategory()),
                    escape(transaction.getLocation()),
                    escape(transaction.getIpAddress()),
                    escape(transaction.getDeviceId()),
                    escape(transaction.getReferenceNumber()),
                    escape(transaction.getStatus()),
                    escape(transaction.getTransactionTime()),
                    escape(transaction.getDescription()),
                    escape(transaction.getRiskScore()),
                    escape(transaction.getTags()),
                    escape(transaction.getCreatedAt()),
                    escape(transaction.getCreatedBy()),
                    escape(transaction.getUpdatedAt()),
                    escape(transaction.getUpdatedBy()));
            
            writer.println(line);
        }

        private String escape(Object value) {
            if (value == null) {
                return "";
            }
            String str = value.toString();
            if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
                return "\"" + str.replace("\"", "\"\"") + "\"";
            }
            return str;
        }

        public void close() {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
