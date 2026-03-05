package com.example.flink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Database initializer for H2 embedded database.
 * Creates schema and populates mock transaction data.
 */
public class DatabaseInitializer {
    
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static final String DB_URL = "jdbc:h2:file:./data/transactions;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "xxxxxxxx";
    private static final int MOCK_DATA_COUNT = 1000;
    
    private static final String[] TRANSACTION_TYPES = {"DEBIT", "CREDIT", "TRANSFER", "PAYMENT", "WITHDRAWAL"};
    private static final String[] CURRENCIES = {"USD", "EUR", "CNY", "GBP", "JPY"};
    private static final String[] CHANNELS = {"WEB", "MOBILE", "ATM", "BRANCH", "API"};
    private static final String[] LOCATIONS = {"US", "CN", "GB", "DE", "JP", "XX", "FR", "AU", "CA", "SG"};
    private static final String[] COUNTERPARTIES = {
        "Amazon", "Walmart", "Alibaba", "Tencent", "Apple", "Microsoft", 
        "Google", "Facebook", "Netflix", "Spotify", "Uber", "Airbnb",
        "Bank of America", "HSBC", "ICBC", "Deutsche Bank", "Unknown_Merchant"
    };
    
    /**
     * Initializes the database with schema and mock data.
     */
    public void initialize() {
        LOG.info("Initializing H2 database...");
        try {
            Class.forName("org.h2.Driver");
            createSchema();
            populateMockData();
            LOG.info("Database initialization completed successfully.");
        } catch (Exception e) {
            LOG.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Creates the transaction table schema.
     */
    private void createSchema() throws SQLException {
        LOG.info("Creating database schema...");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Drop table if exists
            stmt.execute("DROP TABLE IF EXISTS transactions");
            
            // Create transactions table with all necessary fields
            String createTableSql = """
                CREATE TABLE transactions (
                    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    account_id VARCHAR(50) NOT NULL,
                    amount DECIMAL(18, 2) NOT NULL,
                    transaction_type VARCHAR(20) NOT NULL,
                    counterparty VARCHAR(100),
                    transaction_time TIMESTAMP NOT NULL,
                    currency VARCHAR(3) NOT NULL,
                    channel VARCHAR(20) NOT NULL,
                    location VARCHAR(10),
                    description VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            stmt.execute(createTableSql);
            
            // Create index for better query performance
            stmt.execute("CREATE INDEX idx_transaction_time ON transactions(transaction_time)");
            stmt.execute("CREATE INDEX idx_account_id ON transactions(account_id)");
            
            LOG.info("Schema created successfully.");
        }
    }
    
    /**
     * Populates the database with mock transaction data.
     */
    private void populateMockData() throws SQLException {
        LOG.info("Populating mock data ({} records)...", MOCK_DATA_COUNT);
        
        String insertSql = """
            INSERT INTO transactions 
            (account_id, amount, transaction_type, counterparty, transaction_time, 
             currency, channel, location, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            
            Random random = ThreadLocalRandom.current();
            Instant now = Instant.now();
            
            for (int i = 0; i < MOCK_DATA_COUNT; i++) {
                pstmt.setString(1, generateAccountId(random));
                pstmt.setBigDecimal(2, generateAmount(random));
                pstmt.setString(3, TRANSACTION_TYPES[random.nextInt(TRANSACTION_TYPES.length)]);
                pstmt.setString(4, COUNTERPARTIES[random.nextInt(COUNTERPARTIES.length)]);
                pstmt.setTimestamp(5, Timestamp.from(generateTransactionTime(now, random)));
                pstmt.setString(6, CURRENCIES[random.nextInt(CURRENCIES.length)]);
                pstmt.setString(7, CHANNELS[random.nextInt(CHANNELS.length)]);
                pstmt.setString(8, LOCATIONS[random.nextInt(LOCATIONS.length)]);
                pstmt.setString(9, "Transaction " + UUID.randomUUID().toString().substring(0, 8));
                
                pstmt.addBatch();
                
                // Execute batch every 100 records for better performance
                if (i % 100 == 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch();
            LOG.info("Mock data populated successfully.");
        }
    }
    
    private String generateAccountId(Random random) {
        return "ACC" + String.format("%08d", random.nextInt(100000000));
    }
    
    private BigDecimal generateAmount(Random random) {
        // Generate amounts between 0.01 and 100000.00
        double amount = random.nextDouble() * 100000;
        // Make some transactions have large amounts
        if (random.nextDouble() < 0.1) {
            amount = amount * 10;
        }
        return BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP);
    }
    
    private Instant generateTransactionTime(Instant now, Random random) {
        // Generate transactions within the last 30 days
        long daysBack = random.nextInt(30);
        long hoursBack = random.nextInt(24);
        long minutesBack = random.nextInt(60);
        return now.minus(daysBack, ChronoUnit.DAYS)
                   .minus(hoursBack, ChronoUnit.HOURS)
                   .minus(minutesBack, ChronoUnit.MINUTES);
    }
    
    /**
     * Gets the database JDBC URL.
     *
     * @return JDBC URL
     */
    public String getDbUrl() {
        return DB_URL;
    }
    
    /**
     * Gets the database username.
     *
     * @return username
     */
    public String getDbUser() {
        return DB_USER;
    }
    
    /**
     * Gets the database password.
     *
     * @return password
     */
    public String getDbPassword() {
        return DB_PASSWORD;
    }
}
