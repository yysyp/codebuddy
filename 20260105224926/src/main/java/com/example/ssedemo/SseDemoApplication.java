package com.example.ssedemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Spring SSE Demo
 * Demonstrates Server-Sent Events (SSE) implementation with Spring Boot 3
 */
@SpringBootApplication
public class SseDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SseDemoApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("SSE Demo Application Started Successfully!");
        System.out.println("Access the demo at: http://localhost:8080/sse-demo");
        System.out.println("========================================\n");
    }
}
