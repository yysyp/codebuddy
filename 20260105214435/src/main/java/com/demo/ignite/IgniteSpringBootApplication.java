package com.demo.ignite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application Class
 * Apache Ignite Spring Boot Integration Demo
 */
@SpringBootApplication
public class IgniteSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(IgniteSpringBootApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("Ignite Spring Boot Demo Started!");
        System.out.println("========================================");
        System.out.println("Application is running at: http://localhost:8080");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("========================================\n");
    }
}
