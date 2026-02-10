package com.example.transaction;

import com.example.transaction.config.AppConfig;
import com.example.transaction.service.TransactionProcessor;
import java.util.logging.Logger;

public class Application {
    private static final Logger logger = Logger.getLogger(Application.class.getName());
    
    public static void main(String[] args) {
        try {
            logger.info("Starting Transaction Rule Processor Application");
            String traceId = java.util.UUID.randomUUID().toString();
            logger.info("TraceId: " + traceId);
            
            AppConfig config = AppConfig.load();
            TransactionProcessor processor = new TransactionProcessor(config);
            processor.process();
            
            logger.info("Application completed successfully");
        } catch (Exception e) {
            logger.severe("Failed to run application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
