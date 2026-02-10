package com.example.transactionlabeling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for Transaction Labeling with Flink
 */
@SpringBootApplication
@EnableAsync
public class TransactionLabelingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionLabelingApplication.class, args);
    }
}
