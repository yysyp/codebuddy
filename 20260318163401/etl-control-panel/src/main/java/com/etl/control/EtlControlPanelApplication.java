package com.etl.control;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ETL Control Panel Application
 * Main entry point for the rule management and metadata control service
 */
@SpringBootApplication
@EnableScheduling
public class EtlControlPanelApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtlControlPanelApplication.class, args);
    }
}
