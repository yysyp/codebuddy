package com.example.ssedemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for SSE Demo
 * Enables scheduling for background event generation and CORS support
 */
@Configuration
@EnableScheduling
public class SseConfig {

    /**
     * Configure CORS to allow cross-origin requests from client pages
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .maxAge(3600);
                
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "OPTIONS")
                    .allowedHeaders("*");
            }
        };
    }
}
