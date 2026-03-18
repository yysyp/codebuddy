package com.transaction.tagging.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified API response structure for all API endpoints.
 * 
 * Success response:
 * {
 *   "success": true,
 *   "code": "SUCCESS",
 *   "message": "Operation completed successfully",
 *   "timestamp": "2024-01-01T00:00:00Z",
 *   "traceId": "abc123",
 *   "data": {...}
 * }
 * 
 * Failure response:
 * {
 *   "success": false,
 *   "code": "VALIDATION_ERROR",
 *   "message": "Validation failed",
 *   "timestamp": "2024-01-01T00:00:00Z",
 *   "traceId": "abc123",
 *   "data": null,
 *   "errors": [
 *     {"field": "amount", "code": "REQUIRED", "message": "Amount is required"}
 *   ]
 * }
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the request was successful
     */
    private boolean success;

    /**
     * Response code (e.g., SUCCESS, VALIDATION_ERROR, NOT_FOUND)
     */
    private String code;

    /**
     * Human-readable message
     */
    private String message;

    /**
     * Response timestamp in UTC
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Trace ID for request tracing
     */
    private String traceId;

    /**
     * Response data payload
     */
    private T data;

    /**
     * Pagination metadata (optional)
     */
    private PageMeta meta;

    /**
     * Error details (for failure responses)
     */
    private List<FieldError> errors;

    /**
     * Pagination metadata
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMeta {
        private int pageNumber;
        private int pageSize;
        private int totalPages;
        private long totalElements;
    }

    /**
     * Field-level error detail
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String code;
        private String message;
    }

    /**
     * Create a success response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("Operation completed successfully")
                .data(data)
                .build();
    }

    /**
     * Create a success response with data and custom message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Create a success response with pagination
     */
    public static <T> ApiResponse<T> success(T data, PageMeta meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("Operation completed successfully")
                .data(data)
                .meta(meta)
                .build();
    }

    /**
     * Create an error response
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }

    /**
     * Create an error response with field errors
     */
    public static <T> ApiResponse<T> error(String code, String message, List<FieldError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .errors(errors)
                .build();
    }

    /**
     * Add a field error
     */
    public void addError(String field, String code, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(FieldError.builder()
                .field(field)
                .code(code)
                .message(message)
                .build());
    }
}
