package com.example.flink.transaction.source;

import com.example.flink.transaction.config.FlinkConfig;
import com.example.flink.transaction.model.Transaction;
import com.example.flink.transaction.util.TimeUtil;
import com.example.flink.transaction.util.TraceIdUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.*;

/**
 * H2 Database Source Function for Flink
 * Reads transaction data from H2 embedded database
 * Implements thread-safe and resource-safe data access
 */
public class H2TransactionSource extends RichSourceFunction<Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(H2TransactionSource.class);

    private static final long serialVersionUID = 1L;

    private final FlinkConfig config;
    private final long emitIntervalMs;
    private final long maxTransactionsToEmit;
    private transient HikariDataSource dataSource;
    private transient AtomicBoolean isRunning;
    private transient ReentrantLock dbLock;
    private transient Random random;
    private long transactionsEmitted = 0;

    /**
     * Constructor
     *
     * @param config Flink configuration
     * @param emitIntervalMs interval between transaction emissions in milliseconds
     * @param maxTransactionsToEmit maximum number of transactions to emit (0 for unlimited)
     */
    public H2TransactionSource(FlinkConfig config, long emitIntervalMs, long maxTransactionsToEmit) {
        this.config = config;
        this.emitIntervalMs = emitIntervalMs;
        this.maxTransactionsToEmit = maxTransactionsToEmit;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        LOG.info("Opening H2TransactionSource");
        this.isRunning = new AtomicBoolean(true);
        this.dbLock = new ReentrantLock();
        this.random = new Random();

        try {
            // Initialize HikariCP connection pool
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getDatabaseUrl());
            hikariConfig.setUsername(config.getDatabaseUsername());
            hikariConfig.setPassword(config.getDatabasePassword());
            hikariConfig.setMaximumPoolSize(5);
            hikariConfig.setMinimumIdle(1);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setIdleTimeout(600000);
            hikariConfig.setMaxLifetime(1800000);

            this.dataSource = new HikariDataSource(hikariConfig);

            // Initialize database schema
            try (Connection conn = dataSource.getConnection();
                    Statement stmt = conn.createStatement()) {

                // Create table if not exists
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS transactions (" +
                                "transaction_id VARCHAR(100) PRIMARY KEY, " +
                                "customer_id VARCHAR(50) NOT NULL, " +
                                "source_account VARCHAR(50) NOT NULL, " +
                                "destination_account VARCHAR(50) NOT NULL, " +
                                "amount DECIMAL(19,4) NOT NULL, " +
                                "currency VARCHAR(3) NOT NULL, " +
                                "transaction_type VARCHAR(20) NOT NULL, " +
                                "timestamp TIMESTAMP NOT NULL, " +
                                "location_country VARCHAR(2), " +
                                "merchant_category VARCHAR(50), " +
                                "ip_address VARCHAR(45), " +
                                "device_id VARCHAR(50), " +
                                "status VARCHAR(20) DEFAULT 'PENDING', " +
                                "risk_score INT DEFAULT 0" +
                                ")");
                LOG.info("Database schema initialized successfully");
            } catch (SQLException e) {
                LOG.error("Failed to initialize database schema", e);
                throw new RuntimeException("Database initialization failed", e);
            }

            LOG.info("H2 connection pool initialized successfully");
        } catch (Exception e) {
            LOG.error("Failed to initialize H2 connection pool", e);
            throw e;
        }
    }

    @Override
    public void run(SourceContext<Transaction> ctx) throws Exception {
        LOG.info("Starting to emit transactions");

        while (isRunning.get() && (maxTransactionsToEmit == 0 || transactionsEmitted < maxTransactionsToEmit)) {
            try {
                // Generate or fetch transaction
                Transaction transaction = generateOrFetchTransaction();

                if (transaction != null) {
                    // Set trace ID for observability
                    String traceId = TraceIdUtil.generateTraceId();
                    transaction.setTraceId(traceId);
                    TraceIdUtil.setTraceId(traceId);

                    // Collect transaction with context lock
                    synchronized (ctx.getCheckpointLock()) {
                        ctx.collect(transaction);
                    }

                    transactionsEmitted++;
                    LOG.debug("Emitted transaction {}: {}", transaction.getTransactionId(),
                            TimeUtil.formatUtc(transaction.getTimestamp()));
                }

                // Sleep for interval
                if (emitIntervalMs > 0) {
                    Thread.sleep(emitIntervalMs);
                }

            } catch (InterruptedException e) {
                LOG.warn("Source interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOG.error("Error emitting transaction", e);
                // Continue processing, don't stop on single error
                if (emitIntervalMs > 0) {
                    Thread.sleep(emitIntervalMs);
                }
            } finally {
                TraceIdUtil.clearTraceId();
            }
        }

        LOG.info("Finished emitting transactions. Total emitted: {}", transactionsEmitted);
    }

    /**
     * Generate or fetch transaction from database
     *
     * @return Transaction object
     */
    private Transaction generateOrFetchTransaction() {
        dbLock.lock();
        try (Connection conn = dataSource.getConnection()) {
            // First try to read from database
            String query = "SELECT * FROM transactions ORDER BY RANDOM() LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }

            // If no data in database, generate a random transaction
            return generateRandomTransaction(conn);

        } catch (SQLException e) {
            LOG.error("Error accessing database", e);
            return null;
        } finally {
            dbLock.unlock();
        }
    }

    /**
     * Map JDBC ResultSet to Transaction object
     *
     * @param rs the result set
     * @return Transaction object
     * @throws SQLException if mapping fails
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(rs.getString("transaction_id"));
        transaction.setCustomerId(rs.getString("customer_id"));
        transaction.setSourceAccount(rs.getString("source_account"));
        transaction.setDestinationAccount(rs.getString("destination_account"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setCurrency(rs.getString("currency"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setTimestamp(rs.getTimestamp("timestamp").toInstant());
        transaction.setLocationCountry(rs.getString("location_country"));
        transaction.setMerchantCategory(rs.getString("merchant_category"));
        transaction.setIpAddress(rs.getString("ip_address"));
        transaction.setDeviceId(rs.getString("device_id"));
        transaction.setStatus(rs.getString("status"));
        transaction.setRiskScore(rs.getInt("risk_score"));
        return transaction;
    }

    /**
     * Generate a random transaction and insert into database
     *
     * @param conn database connection
     * @return generated transaction
     * @throws SQLException if generation or insertion fails
     */
    private Transaction generateRandomTransaction(Connection conn) throws SQLException {
        String transactionId = "TXN-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
        String customerId = "CUST-" + (random.nextInt(100) + 1);
        String sourceAccount = "ACC-" + (random.nextInt(1000) + 1);
        String destinationAccount = "ACC-" + (random.nextInt(1000) + 1000);
        BigDecimal amount = new BigDecimal(random.nextInt(10000) + 1)
                .add(new BigDecimal(random.nextInt(100)).divide(BigDecimal.valueOf(100)));

        String[] transactionTypes = {"TRANSFER", "PAYMENT", "WITHDRAWAL", "DEPOSIT"};
        String transactionType = transactionTypes[random.nextInt(transactionTypes.length)];

        String[] countries = {"US", "UK", "DE", "FR", "JP", "CN", "AU", "BR", "IN", "CA"};
        String locationCountry = countries[random.nextInt(countries.length)];

        String[] merchantCategories = {"RETAIL", "FOOD", "TRAVEL", "ENTERTAINMENT", "HEALTH", "SERVICES"};
        String merchantCategory = merchantCategories[random.nextInt(merchantCategories.length)];

        String ipAddress = "192.168." + (random.nextInt(256)) + "." + (random.nextInt(256));
        String deviceId = "DEV-" + random.nextInt(10000);

        Transaction transaction = new Transaction(
                transactionId,
                customerId,
                sourceAccount,
                destinationAccount,
                amount,
                "USD",
                transactionType,
                Instant.now()
        );
        transaction.setLocationCountry(locationCountry);
        transaction.setMerchantCategory(merchantCategory);
        transaction.setIpAddress(ipAddress);
        transaction.setDeviceId(deviceId);
        transaction.setStatus("PENDING");
        transaction.setRiskScore(random.nextInt(50)); // Base risk score 0-49

        // Insert into database for consistency
        String insertQuery = "MERGE INTO transactions (transaction_id, customer_id, source_account, " +
                "destination_account, amount, currency, transaction_type, timestamp, location_country, " +
                "merchant_category, ip_address, device_id, status, risk_score) " +
                "KEY(transaction_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, transaction.getTransactionId());
            stmt.setString(2, transaction.getCustomerId());
            stmt.setString(3, transaction.getSourceAccount());
            stmt.setString(4, transaction.getDestinationAccount());
            stmt.setBigDecimal(5, transaction.getAmount());
            stmt.setString(6, transaction.getCurrency());
            stmt.setString(7, transaction.getTransactionType());
            stmt.setTimestamp(8, new java.sql.Timestamp(transaction.getTimestamp().toEpochMilli()));
            stmt.setString(9, transaction.getLocationCountry());
            stmt.setString(10, transaction.getMerchantCategory());
            stmt.setString(11, transaction.getIpAddress());
            stmt.setString(12, transaction.getDeviceId());
            stmt.setString(13, transaction.getStatus());
            stmt.setInt(14, transaction.getRiskScore());
            stmt.executeUpdate();
        }

        LOG.debug("Generated and stored new transaction: {}", transactionId);
        return transaction;
    }

    @Override
    public void cancel() {
        LOG.info("Cancelling H2TransactionSource");
        isRunning.set(false);
    }

    @Override
    public void close() throws Exception {
        LOG.info("Closing H2TransactionSource");
        isRunning.set(false);

        if (dataSource != null) {
            try {
                dataSource.close();
                LOG.info("H2 connection pool closed successfully");
            } catch (Exception e) {
                LOG.error("Error closing H2 connection pool", e);
            }
        }
    }
}
