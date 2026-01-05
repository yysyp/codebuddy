package com.example.ssedemo.controller;

import com.example.ssedemo.model.SseEvent;
import com.example.ssedemo.service.EventSimulationService;
import com.example.ssedemo.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for SSE endpoints and event management
 * Provides endpoints for client connections and manual event triggering
 */
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SseController {

    private final SseEmitterService emitterService;
    private final EventSimulationService eventSimulationService;

    /**
     * SSE endpoint for clients to subscribe to real-time events
     * 
     * @param clientId Optional client identifier (auto-generated if not provided)
     * @param timeout Optional connection timeout in milliseconds (default: 30 minutes)
     * @return SseEmitter for streaming events
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) Long timeout) {
        
        // Generate client ID if not provided
        if (clientId == null || clientId.trim().isEmpty()) {
            clientId = "client-" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        log.info("New SSE subscription request from client: {}", clientId);
        
        return emitterService.createEmitter(clientId, timeout);
    }

    /**
     * Get statistics about active SSE connections
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "activeConnections", emitterService.getActiveConnectionsCount(),
            "timestamp", System.currentTimeMillis(),
            "status", "running"
        ));
    }

    /**
     * Manually trigger a custom event to be broadcasted
     * 
     * @param request Event details
     * @return Success message
     */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, String>> broadcastEvent(@RequestBody Map<String, Object> request) {
        String eventType = (String) request.getOrDefault("eventType", "CUSTOM");
        String message = (String) request.getOrDefault("message", "Custom event");
        Object data = request.get("data");
        
        eventSimulationService.broadcastCustomEvent(eventType, message, data);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Event broadcasted to all clients"
        ));
    }

    /**
     * Disconnect a specific client
     * 
     * @param clientId The client ID to disconnect
     * @return Success message
     */
    @DeleteMapping("/disconnect/{clientId}")
    public ResponseEntity<Map<String, String>> disconnectClient(@PathVariable String clientId) {
        emitterService.removeClient(clientId);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Client disconnected: " + clientId
        ));
    }

    /**
     * Test endpoint to verify the controller is working
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "SSE Demo",
            "activeConnections", String.valueOf(emitterService.getActiveConnectionsCount())
        ));
    }
}
