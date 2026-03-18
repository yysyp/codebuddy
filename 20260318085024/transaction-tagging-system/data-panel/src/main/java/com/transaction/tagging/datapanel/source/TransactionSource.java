package com.transaction.tagging.datapanel.source;

import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.datapanel.config.DataPanelConfig;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Source function for generating transaction data.
 * Can be replaced with Kafka, file, or other sources.
 */
public class TransactionSource implements SourceFunction<Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionSource.class);
    private static final long serialVersionUID = 1L;

    private final int ratePerSecond;
    private final int totalRecords;
    private volatile boolean running = true;

    // Random data generators
    private static final String[] TRANSACTION_TYPES = {
            "TRANSFER", "PAYMENT", "WITHDRAWAL", "DEPOSIT", "PURCHASE"
    };
    private static final String[] MERCHANTS = {
            "Amazon", "Walmart", "Target", "Best Buy", "Starbucks", 
            "McDonald's", "Uber", "Netflix", "Apple", "Google"
    };
    private static final String[] MERCHANT_CATEGORIES = {
            "RETAIL", "FOOD", "ENTERTAINMENT", "TRAVEL", "TECHNOLOGY"
    };
    private static final String[] CHANNELS = {
            "WEB", "MOBILE", "ATM", "POS"
    };
    private static final String[] CURRENCIES = {
            "USD", "EUR", "GBP", "CNY", "JPY"
    };
    private static final String[] LOCATIONS = {
            "New York", "London", "Tokyo", "Singapore", "Sydney"
    };

    private final Random random = new Random();

    public TransactionSource(DataPanelConfig config) {
        this.ratePerSecond = config.getGeneratorRatePerSecond();
        this.totalRecords = config.getGeneratorTotalRecords();
    }

    @Override
    public void run(SourceContext<Transaction> ctx) throws Exception {
        LOG.info("Starting transaction source, rate: {}/s, total: {}", ratePerSecond, totalRecords);
        
        long delayMs = 1000L / ratePerSecond;
        int count = 0;

        while (running && count < totalRecords) {
            Transaction transaction = generateTransaction(count);
            
            synchronized (ctx.getCheckpointLock()) {
                ctx.collect(transaction);
                ctx.emitWatermark(new Watermark(transaction.getTransactionTime().toEpochMilli()));
            }
            
            count++;
            
            if (count % 1000 == 0) {
                LOG.info("Generated {} transactions", count);
            }
            
            // Control rate
            Thread.sleep(delayMs);
        }
        
        LOG.info("Transaction source completed, total records: {}", count);
    }

    @Override
    public void cancel() {
        LOG.info("Canceling transaction source");
        running = false;
    }

    private Transaction generateTransaction(int index) {
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String accountId = "ACC-" + (100000 + random.nextInt(900000));
        
        Instant now = Instant.now();
        
        return Transaction.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .transactionType(TRANSACTION_TYPES[random.nextInt(TRANSACTION_TYPES.length)])
                .amount(BigDecimal.valueOf(10 + random.nextDouble() * 9990).setScale(2, BigDecimal.ROUND_HALF_UP))
                .currency(CURRENCIES[random.nextInt(CURRENCIES.length)])
                .merchantName(MERCHANTS[random.nextInt(MERCHANTS.length)])
                .merchantCategory(MERCHANT_CATEGORIES[random.nextInt(MERCHANT_CATEGORIES.length)])
                .transactionTime(now.minusSeconds(random.nextInt(3600)))
                .location(LOCATIONS[random.nextInt(LOCATIONS.length)])
                .ipAddress(generateIpAddress())
                .deviceId("DEV-" + random.nextInt(10000))
                .channel(CHANNELS[random.nextInt(CHANNELS.length)])
                .status("COMPLETED")
                .description("Transaction " + index)
                .createdAt(now)
                .createdBy("generator")
                .build();
    }

    private String generateIpAddress() {
        return String.format("%d.%d.%d.%d",
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256));
    }

    /**
     * Create source based on configuration.
     */
    public static SourceFunction<Transaction> create(DataPanelConfig config) {
        return switch (config.getSourceType().toLowerCase()) {
            case "generator" -> new TransactionSource(config);
            case "kafka" -> new KafkaTransactionSource(config);
            case "file" -> new FileTransactionSource(config);
            default -> throw new IllegalArgumentException("Unknown source type: " + config.getSourceType());
        };
    }
}
