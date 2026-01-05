package com.example.ssedemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for SSE event messages
 * Represents a real-time event pushed to clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseEvent {

    /**
     * Unique identifier for the event
     */
    private String eventId;

    /**
     * Type or category of the event (e.g., notification, update, alert)
     */
    private String eventType;

    /**
     * Main message content
     */
    private String message;

    /**
     * Optional data payload (JSON formatted)
     */
    private Object data;

    /**
     * Timestamp when the event was created
     */
    private LocalDateTime timestamp;

    /**
     * Create a new event with auto-generated ID and timestamp
     */
    public static SseEvent create(String eventType, String message, Object data) {
        return SseEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a simple event without data payload
     */
    public static SseEvent create(String eventType, String message) {
        return create(eventType, message, null);
    }
}
