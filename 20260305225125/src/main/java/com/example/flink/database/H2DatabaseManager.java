package com.example.flink.database;

import com.example.flink.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages H2 embedded database for transaction data storage.
 * Thread-safe singleton implementation.
 */
public class H2DatabaseManager implements AutoCloseable {
    
    private static final Logger LOG = LoggerFactory.getLogger(H2DatabaseManager.class);
    private static final String DB_URL = "jdbc:h2:file:./data/transaction_db;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "xxxxxxxx";
    
    private static volatile H2DatabaseManager instance;
    private static final ReentrantLock instanceLock = new ReentrantLock();
    
    private final Connection connection;
    private final ReentrantLock connectionLock = new ReentrantLock();
    private volatile boolean initialized = false;
    
    private H2DatabaseManager() throws SQLException {
        LOG.info("Initializing H2 Database Manager...");
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        this.connection.setAutoCommit(false);
        LOG.info("H2 Database connection established");
    }
    
    /**
     * Gets the singleton instance of H2DatabaseManager.
     * Thread-safe lazy initialization.
     */
    public static H2DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instanceLock.lock();
            try {
                if (instance == null) {
                    instance = new H2DatabaseManager();
                }
            } finally {
                instanceLock.unlock();
            }
        }
        return instance;
    }
    
    /**
     * Initializes the database schema and sample data.
     */
    public void initialize() throws SQLException {
        if (initialized) {
            LOG.info("Database already initialized");
            return;
        }
        
        connectionLock.lock();
        try {
            if (!initialized) {
                createSchema();
                insertSampleData();
                connection.commit();
                initialized = true;
                LOG.info("Database initialized successfully");
            }
        } catch (SQLException e) {
            connection.rollback();
            LOG.error("Failed to initialize database: {}", e.getMessage(), e);
            throw e;
        } finally {
            connectionLock.unlock();
        }
    }
    
    /**
     * Creates the database schema.
     */
    private void createSchema() throws SQLException {
        LOG.info("Creating database schema...");
        
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS transactions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                transaction_id VARCHAR(64) NOT NULL UNIQUE,
                account_id VARCHAR(64) NOT NULL,
                counterparty_account VARCHAR(64),
                amount DECIMAL(19, 4) NOT NULL,
                currency VARCHAR(3) NOT NULL DEFAULT 'USD',
                transaction_type VARCHAR(32) NOT NULL,
                channel VARCHAR(32),
                country_code VARCHAR(2),
                transaction_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                description VARCHAR(512),
                risk_level VARCHAR(16),
                tags VARCHAR(256),
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        String createIndexSql1 = """
            CREATE INDEX IF NOT EXISTS idx_transactions_account_id 
            ON transactions(account_id)
            """;
        
        String createIndexSql2 = """
            CREATE INDEX IF NOT EXISTS idx_transactions_transaction_time 
            ON transactions(transaction_time)
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSql);
            stmt.execute(createIndexSql1);
            stmt.execute(createIndexSql2);
            LOG.info("Schema created successfully");
        }
    }
    
    /**
     * Inserts sample transaction data.
     */
    private void insertSampleData() throws SQLException {
        LOG.info("Inserting sample transaction data...");
        
        List<Transaction> sampleTransactions = generateSampleTransactions();
        
        String insertSql = """
            INSERT INTO transactions 
            (transaction_id, account_id, counterparty_account, amount, currency, 
             transaction_type, channel, country_code, transaction_time, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            int batchCount = 0;
            
            for (Transaction tx : sampleTransactions) {
                pstmt.setString(1, tx.getTransactionId());
                pstmt.setString(2, tx.getAccountId());
                pstmt.setString(3, tx.getCounterpartyAccount());
                pstmt.setBigDecimal(4, tx.getAmount());
                pstmt.setString(5, tx.getCurrency());
                pstmt.setString(6, tx.getTransactionType());
                pstmt.setString(7, tx.getChannel());
                pstmt.setString(8, tx.getCountryCode());
                pstmt.setTimestamp(9, Timestamp.from(tx.getTransactionTime()));
                pstmt.setString(10, tx.getDescription());
                
                pstmt.addBatch();
                batchCount++;
                
                if (batchCount % 100 == 0) {
                    pstmt.executeBatch();
                    connection.commit();
                }
            }
            
            pstmt.executeBatch();
            connection.commit();
            LOG.info("Inserted {} sample transactions", sampleTransactions.size());
        }
    }
    
    /**
     * Generates diverse sample transactions for testing.
     */
    private List<Transaction> generateSampleTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        Instant now = Instant.now();
        
        // Regular transactions
        transactions.add(createTransaction("TXN001", "ACC001", new BigDecimal("150.00"), "USD", 
                "PAYMENT", "ONLINE", "US", now.minusSeconds(3600), "Grocery shopping"));
        transactions.add(createTransaction("TXN002", "ACC002", new BigDecimal("2500.00"), "USD", 
                "WIRE_TRANSFER", "BRANCH", "US", now.minusSeconds(7200), "Rent payment"));
        transactions.add(createTransaction("TXN003", "ACC003", new BigDecimal("50.00"), "EUR", 
                "PAYMENT", "MOBILE", "DE", now.minusSeconds(10800), "Coffee shop"));
        
        // High-value transactions
        transactions.add(createTransaction("TXN004", "ACC004", new BigDecimal("150000.00"), "USD", 
                "WIRE_TRANSFER", "BRANCH", "US", now.minusSeconds(14400), "Property purchase"));
        transactions.add(createTransaction("TXN005", "ACC005", new BigDecimal("2500000.00"), "USD", 
                "WIRE_TRANSFER", "BRANCH", "CH", now.minusSeconds(18000), "Investment transfer"));
        
        // Cash transactions
        transactions.add(createTransaction("TXN006", "ACC006", new BigDecimal("75000.00"), "USD", 
                "CASH", "BRANCH", "US", now.minusSeconds(21600), "Cash withdrawal"));
        transactions.add(createTransaction("TXN007", "ACC007", new BigDecimal("5000.00"), "USD", 
                "CASH", "ATM", "US", now.minusSeconds(25200), "ATM withdrawal"));
        
        // Crypto transactions
        transactions.add(createTransaction("TXN008", "ACC008", new BigDecimal("50000.00"), "BTC", 
                "CRYPTO", "ONLINE", "US", now.minusSeconds(28800), "Bitcoin purchase"));
        transactions.add(createTransaction("TXN009", "ACC009", new BigDecimal("25000.00"), "ETH", 
                "CRYPTO", "ONLINE", "US", now.minusSeconds(32400), "Ethereum trade"));
        
        // International transfers
        transactions.add(createTransaction("TXN010", "ACC010", new BigDecimal("10000.00"), "EUR", 
                "INTERNATIONAL_TRANSFER", "ONLINE", "GB", now.minusSeconds(36000), "UK payment"));
        transactions.add(createTransaction("TXN011", "ACC011", new BigDecimal("5000.00"), "USD", 
                "INTERNATIONAL_TRANSFER", "BRANCH", "KY", now.minusSeconds(39600), "Offshore transfer"));
        
        // High-risk country transactions
        transactions.add(createTransaction("TXN012", "ACC012", new BigDecimal("3000.00"), "USD", 
                "WIRE_TRANSFER", "ONLINE", "IR", now.minusSeconds(43200), "Overseas payment"));
        
        // Offshore transactions
        transactions.add(createTransaction("TXN013", "ACC013", new BigDecimal("200000.00"), "USD", 
                "WIRE_TRANSFER", "BRANCH", "BS", now.minusSeconds(46800), "Bahamas investment"));
        transactions.add(createTransaction("TXN014", "ACC014", new BigDecimal("150000.00"), "CHF", 
                "WIRE_TRANSFER", "BRANCH", "CH", now.minusSeconds(50400), "Swiss account deposit"));
        
        // Suspicious descriptions
        transactions.add(createTransaction("TXN015", "ACC015", new BigDecimal("2000.00"), "USD", 
                "PAYMENT", "ONLINE", "US", now.minusSeconds(54000), "Online gambling payment"));
        transactions.add(createTransaction("TXN016", "ACC016", new BigDecimal("5000.00"), "USD", 
                "PAYMENT", "ONLINE", "US", now.minusSeconds(57600), "Casino chips purchase"));
        transactions.add(createTransaction("TXN017", "ACC017", new BigDecimal("3000.00"), "USD", 
                "PAYMENT", "ONLINE", "US", now.minusSeconds(61200), "Government consulting fee"));
        
        // Round amounts
        transactions.add(createTransaction("TXN018", "ACC018", new BigDecimal("10000.00"), "USD", 
                "WIRE_TRANSFER", "ONLINE", "US", now.minusSeconds(64800), "Business payment"));
        transactions.add(createTransaction("TXN019", "ACC019", new BigDecimal("50000.00"), "USD", 
                "WIRE_TRANSFER", "BRANCH", "US", now.minusSeconds(68400), "Investment"));
        
        // Large online wire transfers
        transactions.add(createTransaction("TXN020", "ACC020", new BigDecimal("75000.00"), "USD", 
                "WIRE_TRANSFER", "ONLINE", "US", now.minusSeconds(72000), "Urgent business transfer"));
        transactions.add(createTransaction("TXN021", "ACC021", new BigDecimal("120000.00"), "USD", 
                "WIRE_TRANSFER", "ONLINE", "KY", now.minusSeconds(75600), "Confidential transfer"));
        
        // More variety
        for (int i = 22; i <= 100; i++) {
            String txnId = String.format("TXN%03d", i);
            String accId = String.format("ACC%03d", i);
            BigDecimal amount = BigDecimal.valueOf(Math.random() * 10000 + 100);
            String[] types = {"PAYMENT", "WIRE_TRANSFER", "CASH", "CRYPTO", "INTERNATIONAL_TRANSFER"};
            String[] channels = {"ONLINE", "MOBILE", "ATM", "BRANCH"};
            String[] countries = {"US", "GB", "DE", "FR", "JP", "CA", "AU", "KY", "CH"};
            String[] currencies = {"USD", "EUR", "GBP", "JPY", "CAD"};
            
            String type = types[i % types.length];
            String channel = channels[i % channels.length];
            String country = countries[i % countries.length];
            String currency = currencies[i % currencies.length];
            
            transactions.add(createTransaction(txnId, accId, amount, currency, 
                    type, channel, country, now.minusSeconds(i * 3600), "Transaction description " + i));
        }
        
        return transactions;
    }
    
    private Transaction createTransaction(String txnId, String accId, BigDecimal amount, 
            String currency, String type, String channel, String countryCode, 
            Instant time, String description) {
        return Transaction.builder()
                .transactionId(txnId)
                .accountId(accId)
                .amount(amount)
                .currency(currency)
                .transactionType(type)
                .channel(channel)
                .countryCode(countryCode)
                .transactionTime(time)
                .description(description)
                .build();
    }
    
    /**
     * Retrieves all transactions from the database.
     */
    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        String selectSql = "SELECT * FROM transactions ORDER BY transaction_time";
        
        connectionLock.lock();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            
            LOG.info("Retrieved {} transactions from database", transactions.size());
        } finally {
            connectionLock.unlock();
        }
        
        return transactions;
    }
    
    /**
     * Retrieves a transaction by ID.
     */
    public Optional<Transaction> getTransactionById(String transactionId) throws SQLException {
        String selectSql = "SELECT * FROM transactions WHERE transaction_id = ?";
        
        connectionLock.lock();
        try (PreparedStatement pstmt = connection.prepareStatement(selectSql)) {
            pstmt.setString(1, transactionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        } finally {
            connectionLock.unlock();
        }
        
        return Optional.empty();
    }
    
    /**
     * Updates a transaction with risk level and tags.
     */
    public void updateTransactionRisk(String transactionId, String riskLevel, String tags) 
            throws SQLException {
        String updateSql = """
            UPDATE transactions 
            SET risk_level = ?, tags = ?, updated_at = ?
            WHERE transaction_id = ?
            """;
        
        connectionLock.lock();
        try (PreparedStatement pstmt = connection.prepareStatement(updateSql)) {
            pstmt.setString(1, riskLevel);
            pstmt.setString(2, tags);
            pstmt.setTimestamp(3, Timestamp.from(Instant.now()));
            pstmt.setString(4, transactionId);
            
            pstmt.executeUpdate();
            connection.commit();
            
            LOG.debug("Updated transaction {} with risk level {}", transactionId, riskLevel);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connectionLock.unlock();
        }
    }
    
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("transaction_time");
        Instant transactionTime = ts != null ? ts.toInstant() : Instant.now();
        
        return Transaction.builder()
                .id(rs.getLong("id"))
                .transactionId(rs.getString("transaction_id"))
                .accountId(rs.getString("account_id"))
                .counterpartyAccount(rs.getString("counterparty_account"))
                .amount(rs.getBigDecimal("amount"))
                .currency(rs.getString("currency"))
                .transactionType(rs.getString("transaction_type"))
                .channel(rs.getString("channel"))
                .countryCode(rs.getString("country_code"))
                .transactionTime(transactionTime)
                .description(rs.getString("description"))
                .riskLevel(rs.getString("risk_level"))
                .tags(rs.getString("tags"))
                .build();
    }
    
    @Override
    public void close() throws Exception {
        LOG.info("Closing H2 Database Manager...");
        connectionLock.lock();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOG.info("Database connection closed");
            }
        } finally {
            connectionLock.unlock();
        }
        
        instanceLock.lock();
        try {
            instance = null;
        } finally {
            instanceLock.unlock();
        }
    }
    
    /**
     * Gets the JDBC connection URL for Flink connector.
     */
    public static String getJdbcUrl() {
        return DB_URL;
    }
    
    public static String getDbUser() {
        return DB_USER;
    }
    
    public static String getDbPassword() {
        return DB_PASSWORD;
    }
}
