package com.etl.data.client;

import com.etl.data.dto.ApiResponseDto;
import com.etl.data.dto.RuleDefinitionDto;
import com.etl.data.dto.SqlDefinitionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Control Panel Client
 * HTTP client for communicating with ETL Control Panel
 */
@Slf4j
@Component
public class ControlPanelClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ControlPanelClient(
            @Value("${etl.control-panel.url:http://localhost:8080}") String controlPanelUrl,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper));
                })
                .build();
        
        this.webClient = WebClient.builder()
                .baseUrl(controlPanelUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    /**
     * Get all published rules from Control Panel
     */
    public List<RuleDefinitionDto> getPublishedRules() {
        log.info("Fetching published rules from Control Panel");
        
        try {
            String jsonResponse = webClient.get()
                    .uri("/api/v1/rules/published")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("Raw JSON response: {}", jsonResponse);
            
            // Parse the response
            ApiResponseDto<List<RuleDefinitionDto>> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponseDto<List<RuleDefinitionDto>>>() {}
            );
            
            if (response.isSuccess() && response.getData() != null) {
                log.info("Fetched {} published rules", response.getData().size());
                return response.getData();
            } else {
                log.error("Failed to fetch published rules: {}", response.getMessage());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error fetching published rules", e);
            throw new RuntimeException("Failed to fetch published rules: " + e.getMessage(), e);
        }
    }

    /**
     * Get rule by name from Control Panel
     */
    public RuleDefinitionDto getRuleByName(String name) {
        log.info("Fetching rule by name: {}", name);
        
        try {
            String jsonResponse = webClient.get()
                    .uri("/api/v1/rules/name/{name}", name)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("Raw JSON response: {}", jsonResponse);
            
            // Parse the response
            ApiResponseDto<RuleDefinitionDto> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponseDto<RuleDefinitionDto>>() {}
            );
            
            if (response.isSuccess() && response.getData() != null) {
                log.info("Fetched rule: {}", response.getData().getName());
                return response.getData();
            } else {
                log.error("Failed to fetch rule: {}", response.getMessage());
                throw new RuntimeException("Failed to fetch rule: " + response.getMessage());
            }
        } catch (Exception e) {
            log.error("Error fetching rule by name: {}", name, e);
            throw new RuntimeException("Failed to fetch rule: " + e.getMessage(), e);
        }
    }

    /**
     * Get SQL definition by name from Control Panel
     */
    public SqlDefinitionDto getSqlByName(String name) {
        log.info("Fetching SQL definition by name: {}", name);
        
        try {
            String jsonResponse = webClient.get()
                    .uri("/api/v1/sql/name/{name}", name)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("Raw JSON response: {}", jsonResponse);
            
            // Parse the response
            ApiResponseDto<SqlDefinitionDto> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponseDto<SqlDefinitionDto>>() {}
            );
            
            if (response.isSuccess() && response.getData() != null) {
                log.info("Fetched SQL definition: {}", response.getData().getName());
                return response.getData();
            } else {
                log.error("Failed to fetch SQL definition: {}", response.getMessage());
                throw new RuntimeException("Failed to fetch SQL definition: " + response.getMessage());
            }
        } catch (Exception e) {
            log.error("Error fetching SQL definition by name: {}", name, e);
            throw new RuntimeException("Failed to fetch SQL definition: " + e.getMessage(), e);
        }
    }

    /**
     * Get all active SQL definitions from Control Panel
     */
    public List<SqlDefinitionDto> getActiveSqlDefinitions() {
        log.info("Fetching active SQL definitions from Control Panel");
        
        try {
            String jsonResponse = webClient.get()
                    .uri("/api/v1/sql/active")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("Raw JSON response: {}", jsonResponse);
            
            // Parse the response
            ApiResponseDto<List<SqlDefinitionDto>> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponseDto<List<SqlDefinitionDto>>>() {}
            );
            
            if (response.isSuccess() && response.getData() != null) {
                log.info("Fetched {} active SQL definitions", response.getData().size());
                return response.getData();
            } else {
                log.error("Failed to fetch SQL definitions: {}", response.getMessage());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error fetching active SQL definitions", e);
            throw new RuntimeException("Failed to fetch SQL definitions: " + e.getMessage(), e);
        }
    }

    /**
     * Get SQL definitions by type from Control Panel
     */
    public List<SqlDefinitionDto> getSqlByType(String type) {
        log.info("Fetching SQL definitions by type: {}", type);
        
        try {
            String jsonResponse = webClient.get()
                    .uri("/api/v1/sql/type/{type}/active", type)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("Raw JSON response: {}", jsonResponse);
            
            // Parse the response
            ApiResponseDto<List<SqlDefinitionDto>> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponseDto<List<SqlDefinitionDto>>>() {}
            );
            
            if (response.isSuccess() && response.getData() != null) {
                log.info("Fetched {} SQL definitions by type", response.getData().size());
                return response.getData();
            } else {
                log.error("Failed to fetch SQL definitions: {}", response.getMessage());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error fetching SQL definitions by type: {}", type, e);
            throw new RuntimeException("Failed to fetch SQL definitions: " + e.getMessage(), e);
        }
    }
}
