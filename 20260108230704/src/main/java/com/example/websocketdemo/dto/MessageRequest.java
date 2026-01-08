package com.example.websocketdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Message request object")
public class MessageRequest {

    /**
     * Username of the sender
     */
    @Schema(description = "Username of the sender", example = "john_doe")
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    private String username;

    /**
     * Message content
     */
    @Schema(description = "Message content", example = "Hello, everyone!")
    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    private String content;

    /**
     * Room identifier
     */
    @Schema(description = "Room identifier", example = "general")
    @NotBlank(message = "Room is required")
    @Size(max = 100, message = "Room name must not exceed 100 characters")
    private String room;
}
