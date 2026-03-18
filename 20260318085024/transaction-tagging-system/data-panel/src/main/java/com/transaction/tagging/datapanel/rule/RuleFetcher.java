package com.transaction.tagging.datapanel.rule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.transaction.tagging.common.dto.ApiResponse;
import com.transaction.tagging.common.entity.RuleMetadata;
import com.transaction.tagging.datapanel.config.DataPanelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fetches rules from Control Panel and caches them locally.
 * Periodically refreshes the rules based on configuration.
 */
public class RuleFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(RuleFetcher.class);

    private final DataPanelConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<List<RuleMetadata>> cachedRules;

    public RuleFetcher(DataPanelConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rule-fetcher-scheduler");
            t.setDaemon(true);
            return t;
        });
        this.cachedRules = new AtomicReference<>(Collections.emptyList());
    }

    /**
     * Start the rule fetcher - initial fetch and scheduled refresh.
     */
    public void start() {
        LOG.info("Starting rule fetcher, Control Panel URL: {}", config.getRulesFetchUrl());
        
        // Initial fetch
        fetchRules();
        
        // Schedule periodic refresh
        scheduler.scheduleAtFixedRate(
                this::fetchRules,
                config.getRuleRefreshIntervalSeconds(),
                config.getRuleRefreshIntervalSeconds(),
                TimeUnit.SECONDS
        );
        
        LOG.info("Rule fetcher started, refresh interval: {}s", config.getRuleRefreshIntervalSeconds());
    }

    /**
     * Stop the rule fetcher.
     */
    public void stop() {
        LOG.info("Stopping rule fetcher");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Fetch rules from Control Panel.
     */
    private synchronized void fetchRules() {
        try {
            LOG.debug("Fetching rules from Control Panel: {}", config.getRulesFetchUrl());
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getRulesFetchUrl()))
                    .timeout(Duration.ofSeconds(config.getRuleFetchTimeoutSeconds()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ApiResponse<List<RuleMetadata>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<List<RuleMetadata>>>() {}
                );
                
                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    cachedRules.set(apiResponse.getData());
                    LOG.info("Fetched {} rules from Control Panel", apiResponse.getData().size());
                } else {
                    LOG.warn("Failed to fetch rules: {}", apiResponse.getMessage());
                }
            } else {
                LOG.warn("Failed to fetch rules, status code: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Error fetching rules from Control Panel", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Get cached rules.
     */
    public List<RuleMetadata> getCachedRules() {
        return cachedRules.get();
    }

    /**
     * Force refresh rules.
     */
    public void forceRefresh() {
        fetchRules();
    }
}
