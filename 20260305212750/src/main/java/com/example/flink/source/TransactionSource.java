package com.example.flink.source;

import com.example.flink.model.Transaction;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom source function to read transactions from H2 database.
 * This source reads data in parallel using range-based partitioning.
 */
public class TransactionSource extends RichParallelSourceFunction<Transaction> {
    
    private static final Logger LOG = LoggerFactory.getLogger(TransactionSource.class);
    
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    
    private transient Connection connection;
    private transient PreparedStatement statement;
    private transient ResultSet resultSet;
    private transient AtomicLong counter;
    
    private volatile boolean isRunning = false;
    
    public TransactionSource(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        LOG.info("Opening TransactionSource for subtask {}...", getRuntimeContext().getIndexOfThisSubtask());
        
        // Load H2 driver
        Class.forName("org.h2.Driver");
        
        // Create connection
        this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        this.connection.setAutoCommit(false);
        
        this.counter = new AtomicLong(0);
        this.isRunning = true;
        
        LOG.info("TransactionSource opened successfully for subtask {}", getRuntimeContext().getIndexOfThisSubtask());
    }
    
    @Override
    public void run(SourceContext<Transaction> ctx) throws Exception {
        int subtaskIndex = getRuntimeContext().getIndexOfThisSubtask();
        int parallelism = getRuntimeContext().getNumberOfParallelSubtasks();
        
        LOG.info("Running TransactionSource on subtask {} of {}", subtaskIndex, parallelism);
        
        try {
            // Get total count for partitioning
            long totalCount = getTotalCount();
            
            if (totalCount == 0) {
                LOG.warn("No transactions found in database");
                return;
            }
            
            // Calculate partition for this subtask
            long partitionSize = totalCount / parallelism;
            long startOffset = subtaskIndex * partitionSize;
            long endOffset = (subtaskIndex == parallelism - 1) ? totalCount : (subtaskIndex + 1) * partitionSize;
            
            LOG.info("Subtask {} processing records {} to {} (total: {})", 
                    subtaskIndex, startOffset, endOffset, totalCount);
            
            // Query with LIMIT and OFFSET for this partition
            String query = "SELECT transaction_id, account_id, amount, transaction_type, " +
                          "counterparty, transaction_time, currency, channel, location, description " +
                          "FROM transactions ORDER BY transaction_id LIMIT ? OFFSET ?";
            
            this.statement = connection.prepareStatement(query);
            this.statement.setLong(1, endOffset - startOffset);
            this.statement.setLong(2, startOffset);
            this.statement.setFetchSize(100);
            
            this.resultSet = statement.executeQuery();
            
            while (isRunning && resultSet.next()) {
                Transaction transaction = mapResultSetToTransaction(resultSet);
                
                // Emit with synchronization for thread safety
                synchronized (ctx.getCheckpointLock()) {
                    ctx.collect(transaction);
                }
                
                long count = counter.incrementAndGet();
                if (count % 100 == 0) {
                    LOG.debug("Subtask {} emitted {} transactions", subtaskIndex, count);
                }
            }
            
            LOG.info("Subtask {} finished reading {} transactions", subtaskIndex, counter.get());
            
        } catch (Exception e) {
            LOG.error("Error reading transactions on subtask {}: {}", subtaskIndex, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void cancel() {
        LOG.info("Cancelling TransactionSource on subtask {}...", 
                getRuntimeContext().getIndexOfThisSubtask());
        isRunning = false;
    }
    
    @Override
    public void close() throws Exception {
        LOG.info("Closing TransactionSource on subtask {}...", 
                getRuntimeContext().getIndexOfThisSubtask());
        
        isRunning = false;
        
        // Close resources in reverse order
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOG.warn("Error closing result set", e);
            }
        }
        
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOG.warn("Error closing statement", e);
            }
        }
        
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.warn("Error closing connection", e);
            }
        }
        
        super.close();
    }
    
    /**
     * Gets the total count of transactions for partitioning.
     */
    private long getTotalCount() throws SQLException {
        try (PreparedStatement countStmt = connection.prepareStatement("SELECT COUNT(*) FROM transactions");
             ResultSet countRs = countStmt.executeQuery()) {
            if (countRs.next()) {
                return countRs.getLong(1);
            }
        }
        return 0;
    }
    
    /**
     * Maps a ResultSet row to a Transaction object.
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Timestamp transactionTime = rs.getTimestamp("transaction_time");
        Instant transactionInstant = transactionTime != null ? transactionTime.toInstant() : null;
        
        return Transaction.builder()
                .transactionId(rs.getLong("transaction_id"))
                .accountId(rs.getString("account_id"))
                .amount(rs.getBigDecimal("amount"))
                .transactionType(rs.getString("transaction_type"))
                .counterparty(rs.getString("counterparty"))
                .transactionTime(transactionInstant)
                .currency(rs.getString("currency"))
                .channel(rs.getString("channel"))
                .location(rs.getString("location"))
                .description(rs.getString("description"))
                .build();
    }
}
