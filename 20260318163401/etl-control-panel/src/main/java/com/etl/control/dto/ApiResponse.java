package com.etl.control.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standard API Response
 * Unified response structure for all API endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private Instant timestamp;
    private String traceId;
    private T data;
    private MetaData meta;
    private List<ErrorDetail> errors;

    /**
     * Meta data for pagination
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaData {
        private Integer pageNumber;
        private Integer pageSize;
        private Integer totalPages;
        private Long totalElements;
    }

    /**
     * Error detail for validation errors
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private String field;
        private String code;
        private String message;
    }

    /**
     * Create success response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("Operation completed successfully")
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    /**
     * Create success response with custom message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    /**
     * Create success response with pagination
     */
    public static <T> ApiResponse<T> success(T data, MetaData meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("Operation completed successfully")
                .timestamp(Instant.now())
                .data(data)
                .meta(meta)
                .build();
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create error response with details
     */
    public static <T> ApiResponse<T> error(String code, String message, List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .errors(errors)
                .build();
    }
}
