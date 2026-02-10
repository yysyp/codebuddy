package com.example.transaction.output;

import com.example.transaction.model.Transaction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class CsvWriter {
    private static final Logger logger = Logger.getLogger(CsvWriter.class.getName());
    
    private final String outputDirectory;
    private final ReentrantLock lock = new ReentrantLock();
    
    public CsvWriter(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    public void writeTransactions(List<Transaction> transactions) throws IOException {
        lock.lock();
        try {
            java.nio.file.Path outputPath = Paths.get(outputDirectory);
            Files.createDirectories(outputPath);
            
            String fileName = "transactions_" + java.time.Instant.now().toEpochMilli() + ".csv";
            java.nio.file.Path filePath = outputPath.resolve(fileName);
            
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, 
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                
                writer.write("transactionId,accountNumber,amount,currency,merchantName,merchantCategory,");
                writer.write("transactionTime,location,deviceId,labels,fraudRisk,priority\n");
                
                for (Transaction tx : transactions) {
                    writer.write(tx.getTransactionId() + ",");
                    writer.write(tx.getAccountNumber() + ",");
                    writer.write(tx.getAmount() + ",");
                    writer.write(tx.getCurrency() + ",");
                    writer.write(tx.getMerchantName() + ",");
                    writer.write(tx.getMerchantCategory() + ",");
                    writer.write(tx.getTransactionTime().toEpochMilli() + ",");
                    writer.write(tx.getLocation() + ",");
                    writer.write(tx.getDeviceId() + ",");
                    writer.write("\"" + String.join(";", tx.getLabels()) + "\",");
                    writer.write(tx.getFraudRisk() + ",");
                    writer.write(tx.getPriority() + "\n");
                }
            }
            
            logger.info("Successfully wrote " + transactions.size() + " transactions to " + filePath);
        } finally {
            lock.unlock();
        }
    }
}
