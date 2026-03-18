package com.transaction.tagging.controlpanel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Control Panel Application
 * 
 * This application provides:
 * - Rule management using Drools rule engine
 * - Data schema definition and validation
 * - Metadata management
 * - REST API for rule and schema operations
 */
@SpringBootApplication(scanBasePackages = "com.transaction.tagging")
@EnableAsync
@EnableScheduling
public class ControlPanelApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControlPanelApplication.class, args);
    }
}
