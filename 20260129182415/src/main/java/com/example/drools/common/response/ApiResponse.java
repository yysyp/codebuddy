package com.example.drools.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard API Response wrapper for all endpoints
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
    private Map<String, Object> meta;
    private List<ErrorResponse> errors;

    /**
     * Create success response
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("200")
                .message(message)
                .timestamp(Instant.now())
                .traceId(getTraceId())
                .data(data)
                .build();
    }

    /**
     * Create success response with pagination meta
     */
    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("200")
                .message(message)
                .timestamp(Instant.now())
                .traceId(getTraceId())
                .data(data)
                .meta(meta)
                .build();
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String code, String message, List<ErrorResponse> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .traceId(getTraceId())
                .errors(errors)
                .build();
    }

    /**
     * Create error response with data
     */
    public static <T> ApiResponse<T> error(String code, String message, List<ErrorResponse> errors, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .traceId(getTraceId())
                .errors(errors)
                .data(data)
                .build();
    }

    /**
     * Get traceId from ThreadLocal
     */
    private static String getTraceId() {
        return TraceIdContext.getTraceId();
    }
}
