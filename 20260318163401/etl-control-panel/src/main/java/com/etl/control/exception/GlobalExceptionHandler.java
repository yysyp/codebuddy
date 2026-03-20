package com.etl.control.exception;

import com.etl.control.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 * Handles all exceptions and returns unified API response
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle business exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        log.error("Business exception occurred: {} - {}", ex.getCode(), ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error(ex.getCode(), ex.getMessage());
        response.setTimestamp(Instant.now());
        response.setTraceId(getTraceId(request));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation exception occurred: {}", ex.getMessage(), ex);
        
        List<ApiResponse.ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ApiResponse.ErrorDetail.builder()
                        .field(error.getField())
                        .code("VALIDATION_ERROR")
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        
        ApiResponse<Void> response = ApiResponse.error("VALIDATION_FAILED", 
                "Request validation failed", errors);
        response.setTimestamp(Instant.now());
        response.setTraceId(getTraceId(request));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error("INTERNAL_ERROR", 
                "An unexpected error occurred. Please contact support.");
        response.setTimestamp(Instant.now());
        response.setTraceId(getTraceId(request));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Get trace ID from request
     */
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = java.util.UUID.randomUUID().toString();
        }
        return traceId;
    }
}
