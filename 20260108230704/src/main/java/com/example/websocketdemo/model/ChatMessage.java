package com.example.websocketdemo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Chat message entity
 * Represents a message in the chat system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * Message unique identifier
     */
    private String id;

    /**
     * Username who sent the message
     */
    private String username;

    /**
     * Message content
     */
    private String content;

    /**
     * Message type: JOIN, LEAVE, CHAT
     */
    @Builder.Default
    private MessageType type = MessageType.CHAT;

    /**
     * Room identifier
     */
    private String room;

    /**
     * Timestamp when the message was created (UTC)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    /**
     * Message type enumeration
     */
    public enum MessageType {
        /**
         * User joined the chat room
         */
        JOIN,
        /**
         * User left the chat room
         */
        LEAVE,
        /**
         * Regular chat message
         */
        CHAT
    }
}
