package com.cmdwrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Command Wrapper Service Application
 * 
 * This service provides secure command execution with password placeholder replacement.
 * It supports both Windows and Linux platforms.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CommandWrapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommandWrapperApplication.class, args);
    }
}
