package com.example.flink.util;

import com.example.flink.model.TaggedTransaction;
import com.example.flink.model.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for CSV file operations.
 * Thread-safe for read operations, synchronized for write operations.
 */
@Slf4j
public final class CsvUtils {

    private static final String CSV_DELIMITER = ",";
    private static final String DEFAULT_CURRENCY = "USD";
    private static final String DEFAULT_TRANSACTION_TYPE = "DEBIT";

    private CsvUtils() {
        // Utility class - prevent instantiation
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Read transactions from CSV file
     *
     * @param filePath path to CSV file
     * @return list of transactions
     * @throws IOException if file cannot be read
     */
    public static List<Transaction> readTransactions(String filePath) throws IOException {
        Objects.requireNonNull(filePath, "File path cannot be null");

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }

        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean isHeader = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Transaction transaction = parseTransactionLine(line);
                    if (transaction != null && transaction.isValid()) {
                        transactions.add(transaction);
                    } else {
                        log.warn("Invalid transaction data at line {}: {}", lineNumber, line);
                    }
                } catch (Exception e) {
                    log.error("Error parsing line {}: {}", lineNumber, e.getMessage());
                }
            }
        }

        log.info("Read {} transactions from {}", transactions.size(), filePath);
        return transactions;
    }

    /**
     * Parse a single CSV line into Transaction
     */
    private static Transaction parseTransactionLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] fields = line.split(CSV_DELIMITER, -1); // -1 to keep empty fields

        try {
            Transaction.TransactionBuilder builder = Transaction.builder();

            // Parse fields with safe defaults
            if (fields.length > 0 && !fields[0].trim().isEmpty()) {
                builder.transactionId(fields[0].trim());
            }
            if (fields.length > 1 && !fields[1].trim().isEmpty()) {
                builder.accountId(fields[1].trim());
            }
            if (fields.length > 2 && !fields[2].trim().isEmpty()) {
                builder.amount(new BigDecimal(fields[2].trim()));
            } else {
                builder.amount(BigDecimal.ZERO);
            }
            if (fields.length > 3 && !fields[3].trim().isEmpty()) {
                builder.currency(fields[3].trim());
            } else {
                builder.currency(DEFAULT_CURRENCY);
            }
            if (fields.length > 4 && !fields[4].trim().isEmpty()) {
                builder.transactionType(fields[4].trim().toUpperCase());
            } else {
                builder.transactionType(DEFAULT_TRANSACTION_TYPE);
            }
            if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                builder.counterpartyId(fields[5].trim());
            }
            if (fields.length > 6 && !fields[6].trim().isEmpty()) {
                builder.counterpartyName(fields[6].trim());
            }
            if (fields.length > 7 && !fields[7].trim().isEmpty()) {
                builder.description(fields[7].trim());
            }
            if (fields.length > 8 && !fields[8].trim().isEmpty()) {
                try {
                    builder.transactionTime(Instant.parse(fields[8].trim()));
                } catch (DateTimeParseException e) {
                    builder.transactionTime(Instant.now());
                }
            } else {
                builder.transactionTime(Instant.now());
            }
            if (fields.length > 9 && !fields[9].trim().isEmpty()) {
                builder.countryCode(fields[9].trim().toUpperCase());
            }
            if (fields.length > 10 && !fields[10].trim().isEmpty()) {
                builder.ipAddress(fields[10].trim());
            }
            if (fields.length > 11 && !fields[11].trim().isEmpty()) {
                builder.deviceId(fields[11].trim());
            }
            if (fields.length > 12 && !fields[12].trim().isEmpty()) {
                try {
                    builder.riskScore(Integer.parseInt(fields[12].trim()));
                } catch (NumberFormatException e) {
                    builder.riskScore(0);
                }
            } else {
                builder.riskScore(0);
            }

            builder.tags(new ArrayList<>());

            Transaction transaction = builder.build();
            transaction.initialize(); // Generate traceId and other defaults

            return transaction;

        } catch (Exception e) {
            log.error("Error parsing transaction line: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Write tagged transactions to CSV file
     *
     * @param filePath output file path
     * @param transactions list of tagged transactions
     * @throws IOException if file cannot be written
     */
    public static synchronized void writeTaggedTransactions(
            String filePath,
            List<TaggedTransaction> transactions) throws IOException {

        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(transactions, "Transactions list cannot be null");

        Path path = Paths.get(filePath);

        // Create parent directories if needed
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Write header
            writer.write(String.join(CSV_DELIMITER, TaggedTransaction.getCsvHeader()));
            writer.newLine();

            // Write data rows
            for (TaggedTransaction transaction : transactions) {
                if (transaction != null) {
                    writer.write(formatCsvRow(transaction.toCsvRow()));
                    writer.newLine();
                }
            }
        }

        log.info("Wrote {} tagged transactions to {}", transactions.size(), filePath);
    }

    /**
     * Format CSV row array to string
     */
    private static String formatCsvRow(Object[] row) {
        if (row == null || row.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) {
                sb.append(CSV_DELIMITER);
            }

            String value = (row[i] != null) ? row[i].toString() : "";

            // Escape values containing commas or quotes
            if (value.contains(CSV_DELIMITER) || value.contains("\"")) {
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }

            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * Generate sample transaction data
     *
     * @param count number of transactions to generate
     * @return list of sample transactions
     */
    public static List<Transaction> generateSampleTransactions(int count) {
        List<Transaction> transactions = new ArrayList<>();
        String[] transactionTypes = {"DEBIT", "CREDIT", "TRANSFER", "PAYMENT", "REFUND"};
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "CNY"};
        String[] countries = {"US", "CN", "GB", "DE", "JP", "FR", "AU"};
        String[] counterparties = {"Amazon", "Walmart", "Shell", "Uber", "Netflix", "Unknown"};

        for (int i = 0; i < count; i++) {
            Transaction transaction = Transaction.builder()
                    .transactionId(TraceIdGenerator.generateTransactionId("ACC" + (i % 100)))
                    .accountId("ACC" + (i % 100))
                    .amount(BigDecimal.valueOf(Math.random() * 10000).setScale(2, java.math.RoundingMode.HALF_UP))
                    .currency(currencies[i % currencies.length])
                    .transactionType(transactionTypes[i % transactionTypes.length])
                    .counterpartyId("MERCH" + (i % 50))
                    .counterpartyName(counterparties[i % counterparties.length])
                    .description("Transaction " + i)
                    .transactionTime(Instant.now().minusSeconds((long) (Math.random() * 86400 * 30)))
                    .countryCode(countries[i % countries.length])
                    .ipAddress("192.168." + (i % 256) + "." + (i % 256))
                    .deviceId("DEV" + (i % 20))
                    .riskScore((int) (Math.random() * 100))
                    .tags(new ArrayList<>())
                    .build();

            transaction.initialize();
            transactions.add(transaction);
        }

        return transactions;
    }

    /**
     * Write sample CSV file
     *
     * @param filePath output path
     * @param count number of records
     * @throws IOException if write fails
     */
    public static void writeSampleCsv(String filePath, int count) throws IOException {
        List<Transaction> transactions = generateSampleTransactions(count);

        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Header
            writer.write("transaction_id,account_id,amount,currency,transaction_type," +
                    "counterparty_id,counterparty_name,description,transaction_time," +
                    "country_code,ip_address,device_id,risk_score");
            writer.newLine();

            // Data
            for (Transaction t : transactions) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d",
                        t.getTransactionId(),
                        t.getAccountId(),
                        t.getAmount(),
                        t.getCurrency(),
                        t.getTransactionType(),
                        t.getCounterpartyId(),
                        t.getCounterpartyName(),
                        t.getDescription(),
                        t.getTransactionTime(),
                        t.getCountryCode(),
                        t.getIpAddress(),
                        t.getDeviceId(),
                        t.getRiskScore()));
                writer.newLine();
            }
        }

        log.info("Generated sample CSV with {} records at {}", count, filePath);
    }
}
