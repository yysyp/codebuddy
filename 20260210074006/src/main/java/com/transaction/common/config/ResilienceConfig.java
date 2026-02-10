package com.transaction.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Resilience4j (Circuit Breaker, Rate Limiter, Retry, Time Limiter)
 */
@Slf4j
@Configuration
public class ResilienceConfig {

    @Value("${app.security.circuit-breaker.enabled:true}")
    private boolean circuitBreakerEnabled;

    @Value("${app.security.circuit-breaker.failure-rate-threshold:50}")
    private int failureRateThreshold;

    @Value("${app.security.circuit-breaker.wait-duration-open-millis:60000}")
    private long waitDurationOpenMillis;

    @Value("${app.security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${app.security.rate-limit.permits-per-second:100}")
    private int permitsPerSecond;

    /**
     * Circuit Breaker Configuration
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofMillis(waitDurationOpenMillis))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        log.info("Circuit Breaker initialized with failure threshold: {}%", failureRateThreshold);
        return registry;
    }

    /**
     * Rate Limiter Configuration
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(permitsPerSecond)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        log.info("Rate Limiter initialized with {} permits per second", permitsPerSecond);
        return registry;
    }

    /**
     * Retry Configuration
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryExceptions(Exception.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        log.info("Retry configuration initialized");
        return registry;
    }

    /**
     * Time Limiter Configuration
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);
        log.info("Time Limiter configuration initialized");
        return registry;
    }
}
