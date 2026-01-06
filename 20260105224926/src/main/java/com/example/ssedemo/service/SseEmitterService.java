package com.example.ssedemo.service;

import com.example.ssedemo.model.SseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing Server-Sent Events (SSE) connections and broadcasting
 * Thread-safe implementation using ConcurrentHashMap for managing multiple clients
 */
@Slf4j
@Service
public class SseEmitterService {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    public SseEmitterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Value("${sse.timeout.heartbeat:30000}")
    private long heartbeatInterval;

    /**
     * Initialize heartbeat mechanism to keep connections alive
     */
    @PostConstruct
    public void init() {
        // Schedule heartbeat every 30 seconds to prevent connection timeout
        heartbeatExecutor.scheduleAtFixedRate(
            this::sendHeartbeatToAll,
            heartbeatInterval / 2,
            heartbeatInterval / 2,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Create a new SSE emitter for a client
     * 
     * @param clientId Unique identifier for the client
     * @param timeout Timeout in milliseconds (default: 30 minutes)
     * @return SseEmitter instance
     */
    public SseEmitter createEmitter(String clientId, Long timeout) {
        // Default timeout: 30 minutes
        long timeoutMs = timeout != null ? timeout : 30 * 60 * 1000L;
        
        SseEmitter emitter = new SseEmitter(timeoutMs);
        
        // Clean up on completion or timeout
        emitter.onCompletion(() -> {
            log.info("Emitter completed for client: {}", clientId);
            emitters.remove(clientId);
        });
        
        emitter.onTimeout(() -> {
            log.info("Emitter timed out for client: {}", clientId);
            emitters.remove(clientId);
        });
        
        emitter.onError((ex) -> {
            log.error("Emitter error for client: {}", clientId, ex);
            emitters.remove(clientId);
        });
        
        // Store emitter
        emitters.put(clientId, emitter);
        
        // Send initial connection event
        try {
            SseEvent event = SseEvent.create("CONNECTION", "Connected to SSE server", 
                Map.of("clientId", clientId, "timestamp", LocalDateTime.now().toString()));
            sendToClient(clientId, event);
        } catch (Exception e) {
            log.error("Error sending initial connection event", e);
        }
        
        log.info("Created new SSE emitter for client: {}, total active connections: {}", 
            clientId, emitters.size());
        
        return emitter;
    }

    /**
     * Broadcast an event to all connected clients
     * 
     * @param event The event to broadcast
     */
    public void broadcast(SseEvent event) {
        log.info("Broadcasting event {} to {} clients", event.getEventType(), emitters.size());
        
        emitters.forEach((clientId, emitter) -> {
            try {
                sendToClient(clientId, emitter, event);
            } catch (Exception e) {
                log.error("Error broadcasting to client: {}", clientId, e);
                emitters.remove(clientId);
            }
        });
    }

    /**
     * Send an event to a specific client
     * 
     * @param clientId The target client ID
     * @param event The event to send
     */
    public void sendToClient(String clientId, SseEvent event) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            sendToClient(clientId, emitter, event);
        } else {
            log.warn("Client not found: {}", clientId);
        }
    }

    /**
     * Internal method to send event via SseEmitter
     */
    private void sendToClient(String clientId, SseEmitter emitter, SseEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            emitter.send(SseEmitter.event()
                //.name(event.getEventType())
                .id(event.getEventId())
                .data(eventData)
                .reconnectTime(1000));
            
            log.debug("Sent event {} to client: {}", event.getEventType(), clientId);
        } catch (IOException e) {
            log.error("IO Error sending event to client: {}", clientId, e);
            emitter.completeWithError(e);
            emitters.remove(clientId);
        }
    }

    /**
     * Send heartbeat to keep connections alive
     */
    private void sendHeartbeatToAll() {
        SseEvent heartbeat = SseEvent.builder()
            .eventType("HEARTBEAT")
            .message("Keep-alive")
            .timestamp(LocalDateTime.now())
            .build();
        
        emitters.forEach((clientId, emitter) -> {
            try {
                String data = objectMapper.writeValueAsString(heartbeat);
                emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data(data));
            } catch (Exception e) {
                log.warn("Failed to send heartbeat to client: {}", clientId);
                emitters.remove(clientId);
            }
        });
    }

    /**
     * Get count of active connections
     */
    public int getActiveConnectionsCount() {
        return emitters.size();
    }

    /**
     * Remove a specific client connection
     */
    public void removeClient(String clientId) {
        SseEmitter emitter = emitters.remove(clientId);
        if (emitter != null) {
            emitter.complete();
            log.info("Removed client: {}", clientId);
        }
    }

    /**
     * Cleanup on bean destruction
     */
    public void destroy() {
        log.info("Shutting down SSE emitter service...");
        heartbeatExecutor.shutdown();
        emitters.forEach((id, emitter) -> emitter.complete());
        emitters.clear();
    }
}
