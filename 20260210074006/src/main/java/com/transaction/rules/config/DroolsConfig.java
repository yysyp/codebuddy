package com.transaction.rules.config;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for Drools Rule Engine
 */
@Slf4j
@Configuration
public class DroolsConfig {

    @Value("${app.drools.rules-path:classpath:rules/}")
    private String rulesPath;

    @Value("${app.drools.reload-interval:30000}")
    private long reloadInterval;

    private KieContainer kieContainer;
    private final ConcurrentHashMap<String, KieSession> sessionCache = new ConcurrentHashMap<>();
    private ScheduledExecutorService ruleReloader;
    private long lastModifiedTime = 0;

    @Bean
    @CircuitBreaker(name = "droolsCircuitBreaker", fallbackMethod = "getKieContainerFallback")
    @RateLimiter(name = "droolsRateLimiter")
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // Load all .drl files from rules directory
        List<Path> ruleFiles = loadRuleFiles();
        for (Path ruleFile : ruleFiles) {
            log.info("Loading rule file: {}", ruleFile);
            kieFileSystem.write("src/main/resources/" + ruleFile.getFileName().toString(),
                    kieServices.getResources().newClassPathResource("rules/" + ruleFile.getFileName().toString()));
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            log.error("Error building Drools rules: {}", kieBuilder.getResults().getMessages());
            throw new RuntimeException("Error building Drools rules");
        }

        KieModule kieModule = kieBuilder.getKieModule();
        this.kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());

        // Start rule reloader
        startRuleReloader();

        log.info("Drools KieContainer initialized successfully");
        return this.kieContainer;
    }

    @Bean
    public KieSession kieSession() {
        return kieContainer.newKieSession();
    }

    @Bean(destroyMethod = "dispose")
    public KieSession statelessKieSession() {
        return kieContainer.newKieSession();
    }

    /**
     * Get or create cached KieSession
     */
    public KieSession getCachedSession(String sessionId) {
        return sessionCache.computeIfAbsent(sessionId, id -> kieContainer.newKieSession());
    }

    /**
     * Clear session cache
     */
    public void clearSessionCache() {
        sessionCache.values().forEach(KieSession::dispose);
        sessionCache.clear();
        log.info("KieSession cache cleared");
    }

    /**
     * Reload rules dynamically
     */
    public synchronized void reloadRules() {
        log.info("Reloading Drools rules...");
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            List<Path> ruleFiles = loadRuleFiles();
            for (Path ruleFile : ruleFiles) {
                kieFileSystem.write("src/main/resources/" + ruleFile.getFileName().toString(),
                        kieServices.getResources().newClassPathResource("rules/" + ruleFile.getFileName().toString()));
            }

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                log.error("Error reloading Drools rules: {}", kieBuilder.getResults().getMessages());
                return;
            }

            KieModule kieModule = kieBuilder.getKieModule();
            KieContainer newContainer = kieServices.newKieContainer(kieModule.getReleaseId());
            KieContainer oldContainer = this.kieContainer;
            this.kieContainer = newContainer;

            // Clear session cache after reload
            clearSessionCache();

            log.info("Drools rules reloaded successfully");
        } catch (Exception e) {
            log.error("Failed to reload Drools rules", e);
        }
    }

    /**
     * Load rule files from rules directory
     */
    private List<Path> loadRuleFiles() {
        try {
            Path rulesDir = Paths.get("src/main/resources/rules");
            if (!Files.exists(rulesDir)) {
                Files.createDirectories(rulesDir);
                log.info("Created rules directory: {}", rulesDir);
            }

            List<Path> ruleFiles = Files.list(rulesDir)
                    .filter(path -> path.toString().endsWith(".drl"))
                    .toList();

            log.info("Found {} rule files", ruleFiles.size());
            return ruleFiles;
        } catch (Exception e) {
            log.error("Error loading rule files", e);
            return List.of();
        }
    }

    /**
     * Start rule reloader
     */
    private void startRuleReloader() {
        if (ruleReloader == null || ruleReloader.isShutdown()) {
            ruleReloader = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "drools-rule-reloader");
                thread.setDaemon(true);
                return thread;
            });

            ruleReloader.scheduleAtFixedRate(
                    this::checkAndReloadRules,
                    reloadInterval,
                    reloadInterval,
                    TimeUnit.MILLISECONDS
            );

            log.info("Rule reloader started with interval: {}ms", reloadInterval);
        }
    }

    /**
     * Check and reload rules if files changed
     */
    private void checkAndReloadRules() {
        try {
            List<Path> ruleFiles = loadRuleFiles();
            long currentMaxModified = ruleFiles.stream()
                    .mapToLong(file -> {
                        try {
                            return Files.getLastModifiedTime(file).toMillis();
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);

            if (currentMaxModified > lastModifiedTime) {
                log.info("Rule files modified, reloading...");
                lastModifiedTime = currentMaxModified;
                reloadRules();
            }
        } catch (Exception e) {
            log.error("Error checking rule file modifications", e);
        }
    }

    /**
     * Fallback method for circuit breaker
     */
    private KieContainer getKieContainerFallback(Exception e) {
        log.error("Drools circuit breaker triggered, using cached container", e);
        return this.kieContainer;
    }

    /**
     * Cleanup on shutdown
     */
    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up Drools resources...");
        if (ruleReloader != null && !ruleReloader.isShutdown()) {
            ruleReloader.shutdown();
            try {
                if (!ruleReloader.awaitTermination(5, TimeUnit.SECONDS)) {
                    ruleReloader.shutdownNow();
                }
            } catch (InterruptedException e) {
                ruleReloader.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        clearSessionCache();
    }
}
