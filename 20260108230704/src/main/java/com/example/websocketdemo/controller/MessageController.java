package com.example.websocketdemo.controller;

import com.example.websocketdemo.handler.ChatWebSocketHandler;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for WebSocket related endpoints
 * Provides information about WebSocket connections and rooms
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "WebSocket Management", description = "APIs for managing WebSocket connections")
public class MessageController {

    private final ChatWebSocketHandler chatWebSocketHandler;

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint", description = "Check if the application is running")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is healthy")
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("application", "websocket-demo");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/websocket/info")
    @Operation(summary = "Get WebSocket information", description = "Get information about available WebSocket endpoints")
    @RateLimiter(name = "websocketRateLimiter", fallbackMethod = "rateLimitFallback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WebSocket information retrieved successfully"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<Map<String, Object>> getWebSocketInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("echoEndpoint", "/ws/echo");
        info.put("chatEndpoint", "/ws/chat");
        info.put("description", "WebSocket Demo Application");
        info.put("echoDescription", "Simple echo server that returns messages with timestamps");
        info.put("chatDescription", "Chat room functionality with support for multiple rooms");

        log.debug("WebSocket info requested");
        return ResponseEntity.ok(info);
    }

    /**
     * Fallback method for rate limiting
     */
    public ResponseEntity<Map<String, Object>> rateLimitFallback(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Rate limit exceeded. Please try again later.");
        response.put("timestamp", System.currentTimeMillis());
        log.warn("Rate limit exceeded: {}", e.getMessage());
        return ResponseEntity.status(429).body(response);
    }
}
