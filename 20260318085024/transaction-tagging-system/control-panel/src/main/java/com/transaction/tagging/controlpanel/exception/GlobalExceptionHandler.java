package com.transaction.tagging.controlpanel.exception;

import com.transaction.tagging.common.dto.ApiResponse;
import com.transaction.tagging.common.exception.BusinessException;
import com.transaction.tagging.common.exception.ErrorCode;
import com.transaction.tagging.common.util.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Control Panel application.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: code={}, message={}", ex.getCode(), ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.valueOf(ex.getHttpStatus()))
                .body(ApiResponse.<Void>error(ex.getCode(), ex.getMessage())
                        .toBuilder()
                        .traceId(TraceContext.getTraceId())
                        .build());
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation exception: {}", ex.getMessage());
        
        List<ApiResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ApiResponse.FieldError.builder()
                        .field(error.getField())
                        .code(error.getCode())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>error(ErrorCode.VALIDATION_ERROR.getCode(), 
                        "Validation failed", errors)
                        .toBuilder()
                        .traceId(TraceContext.getTraceId())
                        .build());
    }

    /**
     * Handle type mismatch errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch exception: {}", ex.getMessage());
        
        String message = String.format("Parameter '%s' should be of type '%s'", 
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>error(ErrorCode.INVALID_REQUEST.getCode(), message)
                        .toBuilder()
                        .traceId(TraceContext.getTraceId())
                        .build());
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>error(ErrorCode.INVALID_REQUEST.getCode(), ex.getMessage())
                        .toBuilder()
                        .traceId(TraceContext.getTraceId())
                        .build());
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected exception", ex);
        
        // Don't expose internal error details to client
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>error(ErrorCode.UNKNOWN_ERROR.getCode(), 
                        "An unexpected error occurred")
                        .toBuilder()
                        .traceId(TraceContext.getTraceId())
                        .build());
    }
}
