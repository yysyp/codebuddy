package com.transaction.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standard API Response wrapper for successful responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API Response")
public class ApiResponse<T> {

    @Schema(description = "Indicates if the request was successful")
    private boolean success;

    @Schema(description = "Response code")
    private String code;

    @Schema(description = "Response message")
    private String message;

    @Schema(description = "Response timestamp in UTC")
    private Instant timestamp;

    @Schema(description = "Trace ID for request tracking")
    private String traceId;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Pagination metadata")
    private PaginationMeta meta;

    /**
     * Create a successful response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("200")
                .message("Success")
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    /**
     * Create a successful response with message
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("200")
                .message(message)
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    /**
     * Create a successful response with pagination
     */
    public static <T> ApiResponse<T> success(T data, PaginationMeta meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("200")
                .message("Success")
                .timestamp(Instant.now())
                .data(data)
                .meta(meta)
                .build();
    }
}
