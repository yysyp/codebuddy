package com.example.websocketdemo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for message operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Message response object")
public class MessageResponse {

    /**
     * Message unique identifier
     */
    @Schema(description = "Message unique identifier")
    private String id;

    /**
     * Username of the sender
     */
    @Schema(description = "Username of the sender")
    private String username;

    /**
     * Message content
     */
    @Schema(description = "Message content")
    private String content;

    /**
     * Message type
     */
    @Schema(description = "Message type (JOIN, LEAVE, CHAT)")
    private String type;

    /**
     * Room identifier
     */
    @Schema(description = "Room identifier")
    private String room;

    /**
     * Timestamp when the message was created (UTC)
     */
    @Schema(description = "Timestamp when the message was created (UTC)")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    /**
     * Success status
     */
    @Schema(description = "Operation success status")
    private boolean success;

    /**
     * Error message if any
     */
    @Schema(description = "Error message if operation failed")
    private String error;
}
