package com.etl.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ETL Data Panel Application
 * Main entry point for the data processing service
 */
@SpringBootApplication
public class EtlDataPanelApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtlDataPanelApplication.class, args);
    }
}
