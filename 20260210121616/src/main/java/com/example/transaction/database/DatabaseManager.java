package com.example.transaction.database;

import com.example.transaction.config.AppConfig;
import com.example.transaction.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    
    private final AppConfig config;
    private Connection connection;
    private final ReentrantLock lock = new ReentrantLock();
    
    public DatabaseManager(AppConfig config) {
        this.config = config;
        initialize();
    }
    
    private void initialize() {
        lock.lock();
        try {
            Class.forName(config.getDbDriver());
            connection = DriverManager.getConnection(
                config.getDbUrl(),
                config.getDbUsername(),
                config.getDbPassword()
            );
            createSchema();
            insertSampleData();
            logger.info("Database initialized successfully");
        } catch (Exception e) {
            logger.severe("Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        } finally {
            lock.unlock();
        }
    }
    
    private void createSchema() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id VARCHAR(36) PRIMARY KEY,
                account_number VARCHAR(20) NOT NULL,
                amount DECIMAL(19,2) NOT NULL,
                currency VARCHAR(3) NOT NULL,
                merchant_name VARCHAR(100),
                merchant_category VARCHAR(50),
                transaction_time TIMESTAMP NOT NULL,
                location VARCHAR(100),
                device_id VARCHAR(50)
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("Schema created successfully");
        }
    }
    
    private void insertSampleData() throws SQLException {
        String checkSQL = "SELECT COUNT(*) FROM transactions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSQL)) {
            if (rs.next() && rs.getInt(1) > 0) {
                logger.info("Sample data already exists");
                return;
            }
        }
        
        String insertSQL = """
            INSERT INTO transactions (transaction_id, account_number, amount, currency, 
                merchant_name, merchant_category, transaction_time, location, device_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        Instant baseTime = Instant.now();
        List<Transaction> sampleTransactions = List.of(
            Transaction.builder()
                .transactionId("T001")
                .accountNumber("ACC001")
                .amount(new BigDecimal("5000.00"))
                .currency("USD")
                .merchantName("Luxury Store")
                .merchantCategory("Retail")
                .transactionTime(baseTime.minusSeconds(3600))
                .location("New York")
                .deviceId("DEV001")
                .build(),
            Transaction.builder()
                .transactionId("T002")
                .accountNumber("ACC001")
                .amount(new BigDecimal("15000.00"))
                .currency("USD")
                .merchantName("Electronics Shop")
                .merchantCategory("Electronics")
                .transactionTime(baseTime.minusSeconds(1800))
                .location("New York")
                .deviceId("DEV001")
                .build(),
            Transaction.builder()
                .transactionId("T003")
                .accountNumber("ACC002")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantName("Grocery Store")
                .merchantCategory("Grocery")
                .transactionTime(baseTime.minusSeconds(900))
                .location("Los Angeles")
                .deviceId("DEV002")
                .build(),
            Transaction.builder()
                .transactionId("T004")
                .accountNumber("ACC003")
                .amount(new BigDecimal("25000.00"))
                .currency("USD")
                .merchantName("Jewelry Store")
                .merchantCategory("Luxury")
                .transactionTime(baseTime.minusSeconds(600))
                .location("Chicago")
                .deviceId("DEV003")
                .build(),
            Transaction.builder()
                .transactionId("T005")
                .accountNumber("ACC001")
                .amount(new BigDecimal("7500.00"))
                .currency("USD")
                .merchantName("Hotel")
                .merchantCategory("Travel")
                .transactionTime(baseTime.minusSeconds(300))
                .location("Miami")
                .deviceId("DEV001")
                .build(),
            Transaction.builder()
                .transactionId("T006")
                .accountNumber("ACC004")
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .merchantName("Coffee Shop")
                .merchantCategory("Food")
                .transactionTime(baseTime.minusSeconds(150))
                .location("Seattle")
                .deviceId("DEV004")
                .build(),
            Transaction.builder()
                .transactionId("T007")
                .accountNumber("ACC003")
                .amount(new BigDecimal("20000.00"))
                .currency("USD")
                .merchantName("Car Dealership")
                .merchantCategory("Automotive")
                .transactionTime(baseTime.minusSeconds(100))
                .location("Chicago")
                .deviceId("DEV003")
                .build(),
            Transaction.builder()
                .transactionId("T008")
                .accountNumber("ACC002")
                .amount(new BigDecimal("300.00"))
                .currency("USD")
                .merchantName("Restaurant")
                .merchantCategory("Food")
                .transactionTime(baseTime.minusSeconds(50))
                .location("Los Angeles")
                .deviceId("DEV002")
                .build(),
            Transaction.builder()
                .transactionId("T009")
                .accountNumber("ACC005")
                .amount(new BigDecimal("12000.00"))
                .currency("USD")
                .merchantName("Online Retailer")
                .merchantCategory("E-commerce")
                .transactionTime(baseTime.minusSeconds(30))
                .location("Unknown")
                .deviceId("DEV005")
                .build(),
            Transaction.builder()
                .transactionId("T010")
                .accountNumber("ACC001")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .merchantName("ATM Withdrawal")
                .merchantCategory("Banking")
                .transactionTime(baseTime.minusSeconds(10))
                .location("New York")
                .deviceId("DEV001")
                .build()
        );
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            for (Transaction tx : sampleTransactions) {
                pstmt.setString(1, tx.getTransactionId());
                pstmt.setString(2, tx.getAccountNumber());
                pstmt.setBigDecimal(3, tx.getAmount());
                pstmt.setString(4, tx.getCurrency());
                pstmt.setString(5, tx.getMerchantName());
                pstmt.setString(6, tx.getMerchantCategory());
                pstmt.setTimestamp(7, Timestamp.from(tx.getTransactionTime()));
                pstmt.setString(8, tx.getLocation());
                pstmt.setString(9, tx.getDeviceId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            logger.info("Inserted " + sampleTransactions.size() + " sample transactions");
        }
    }
    
    public List<Transaction> getAllTransactions() {
        lock.lock();
        try {
            List<Transaction> transactions = new ArrayList<>();
            String sql = "SELECT * FROM transactions ORDER BY transaction_time";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Transaction tx = Transaction.builder()
                        .transactionId(rs.getString("transaction_id"))
                        .accountNumber(rs.getString("account_number"))
                        .amount(rs.getBigDecimal("amount"))
                        .currency(rs.getString("currency"))
                        .merchantName(rs.getString("merchant_name"))
                        .merchantCategory(rs.getString("merchant_category"))
                        .transactionTime(rs.getTimestamp("transaction_time").toInstant())
                        .location(rs.getString("location"))
                        .deviceId(rs.getString("device_id"))
                        .build();
                    transactions.add(tx);
                }
            }
            logger.info("Retrieved " + transactions.size() + " transactions from database");
            return transactions;
        } catch (SQLException e) {
            logger.severe("Failed to retrieve transactions: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve transactions", e);
        } finally {
            lock.unlock();
        }
    }
    
    public void close() {
        lock.lock();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.severe("Failed to close database connection: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
