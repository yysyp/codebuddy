package com.transaction.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standard API Error Response for failed requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API Error Response")
public class ErrorResponse {

    @Schema(description = "Indicates if the request was successful")
    private boolean success;

    @Schema(description = "Error code")
    private String code;

    @Schema(description = "Error message")
    private String message;

    @Schema(description = "Error timestamp in UTC")
    private Instant timestamp;

    @Schema(description = "Trace ID for request tracking")
    private String traceId;

    @Schema(description = "Error details")
    private Object data;

    @Schema(description = "Field-level errors")
    private List<ValidationError> errors;

    /**
     * Create an error response
     */
    public static ErrorResponse error(String code, String message) {
        return ErrorResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with details
     */
    public static ErrorResponse error(String code, String message, Object data) {
        return ErrorResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    /**
     * Create an error response with field validation errors
     */
    public static ErrorResponse error(String code, String message, List<ValidationError> errors) {
        return ErrorResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .errors(errors)
                .build();
    }
}
