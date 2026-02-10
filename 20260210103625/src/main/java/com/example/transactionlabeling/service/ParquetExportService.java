package com.example.transactionlabeling.service;

import com.example.transactionlabeling.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for exporting transaction data to Parquet format
 */

@Service
public class ParquetExportService {

    private static final Logger log = LoggerFactory.getLogger(ParquetExportService.class);

    private static final String PARQUET_SCHEMA =
            "message Transaction {\n" +
            "  required binary transaction_id (UTF8);\n" +
            "  required binary account_number (UTF8);\n" +
            "  required double amount;\n" +
            "  required binary currency (UTF8);\n" +
            "  optional binary transaction_type (UTF8);\n" +
            "  optional binary merchant_category (UTF8);\n" +
            "  optional binary location (UTF8);\n" +
            "  optional binary country_code (UTF8);\n" +
            "  optional double risk_score;\n" +
            "  optional binary status (UTF8);\n" +
            "  optional binary description (UTF8);\n" +
            "  repeated binary labels (UTF8);\n" +
            "  optional int64 processed_at;\n" +
            "  optional int64 created_at;\n" +
            "  optional int64 updated_at;\n" +
            "}";

    /**
     * Export transactions to Parquet format
     */
    public String exportTransactionsToParquet(List<Transaction> transactions, String outputDirectory) throws IOException {
        // Create output directory if it doesn't exist
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // Generate output filename with timestamp
        String timestamp = Instant.now().toString().replace(":", "-");
        String filename = "transactions_" + timestamp + ".parquet";
        String fullPath = outputPath.resolve(filename).toString();

        log.info("Exporting {} transactions to Parquet file: {}", transactions.size(), fullPath);

        Configuration configuration = new Configuration();
        MessageType schema = MessageTypeParser.parseMessageType(PARQUET_SCHEMA);

        try (SimpleParquetWriter writer = new SimpleParquetWriter(new File(fullPath), schema, configuration)) {
            for (Transaction transaction : transactions) {
                writer.write(transaction);
            }
        }

        log.info("Successfully exported transactions to: {}", fullPath);
        return fullPath;
    }

    /**
     * Simple Parquet writer for transactions
     */
    private static class SimpleParquetWriter extends ParquetWriter<Transaction> {
        private final MessageType schema;

        public SimpleParquetWriter(File file, MessageType schema, Configuration configuration) throws IOException {
            super(file,
                  new TransactionWriteSupport(schema),
                  CompressionCodecName.SNAPPY,
                  1024 * 1024, // row group size
                  1024 * 1024, // page size
                  1024, // dictionary page size
                  true, // enable dictionary
                  false, // validation
                  ParquetWriter.DEFAULT_WRITER_VERSION,
                  configuration);
            this.schema = schema;
        }
    }

    /**
     * WriteSupport for Transaction objects
     */
    private static class TransactionWriteSupport extends WriteSupport<Transaction> {
        private final MessageType schema;
        private RecordBuilder recordBuilder;

        public TransactionWriteSupport(MessageType schema) {
            this.schema = schema;
        }

        @Override
        public org.apache.parquet.io.api.RecordConsumer init(Configuration configuration) {
            this.recordBuilder = new RecordBuilder(schema);
            return recordBuilder;
        }

        @Override
        public void write(Transaction record) {
            recordBuilder.startMessage();
            recordBuilder.addField("transaction_id", record.getTransactionId());
            recordBuilder.addField("account_number", record.getAccountNumber());
            recordBuilder.addField("amount", record.getAmount().doubleValue());
            recordBuilder.addField("currency", record.getCurrency());

            if (record.getTransactionType() != null) {
                recordBuilder.addField("transaction_type", record.getTransactionType());
            }
            if (record.getMerchantCategory() != null) {
                recordBuilder.addField("merchant_category", record.getMerchantCategory());
            }
            if (record.getLocation() != null) {
                recordBuilder.addField("location", record.getLocation());
            }
            if (record.getCountryCode() != null) {
                recordBuilder.addField("country_code", record.getCountryCode());
            }
            if (record.getRiskScore() != null) {
                recordBuilder.addField("risk_score", record.getRiskScore().doubleValue());
            }
            if (record.getStatus() != null) {
                recordBuilder.addField("status", record.getStatus());
            }
            if (record.getDescription() != null) {
                recordBuilder.addField("description", record.getDescription());
            }

            // Add labels
            if (record.getLabels() != null) {
                for (String label : record.getLabels()) {
                    recordBuilder.addRepeatedField("labels", label);
                }
            }

            if (record.getProcessedAt() != null) {
                recordBuilder.addField("processed_at", record.getProcessedAt().toEpochMilli());
            }
            if (record.getCreatedAt() != null) {
                recordBuilder.addField("created_at", record.getCreatedAt().toEpochMilli());
            }
            if (record.getUpdatedAt() != null) {
                recordBuilder.addField("updated_at", record.getUpdatedAt().toEpochMilli());
            }

            recordBuilder.endMessage();
        }

        @Override
        public String getName() {
            return "TransactionWriteSupport";
        }
    }

    /**
     * Simple record builder for Parquet
     */
    private static class RecordBuilder implements org.apache.parquet.io.api.RecordConsumer {
        private final MessageType schema;

        public RecordBuilder(MessageType schema) {
            this.schema = schema;
        }

        @Override
        public void startMessage() {
        }

        @Override
        public void endMessage() {
        }

        @Override
        public void startField(String field, int index) {
        }

        @Override
        public void endField(String field, int index) {
        }

        @Override
        public void startGroup() {
        }

        @Override
        public void endGroup() {
        }

        @Override
        public void addInteger(int value) {
        }

        @Override
        public void addLong(long value) {
        }

        @Override
        public void addBoolean(boolean value) {
        }

        @Override
        public void addBinary(Binary value) {
        }

        @Override
        public void addFloat(float value) {
        }

        @Override
        public void addDouble(double value) {
        }

        public void addField(String fieldName, String value) {
            addBinary(Binary.fromString(value));
        }

        public void addField(String fieldName, double value) {
            addDouble(value);
        }

        public void addField(String fieldName, long value) {
            addLong(value);
        }

        public void addRepeatedField(String fieldName, String value) {
            addBinary(Binary.fromString(value));
        }
    }
}
