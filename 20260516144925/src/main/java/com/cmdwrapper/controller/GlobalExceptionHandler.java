package com.cmdwrapper.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        String traceId = UUID.randomUUID().toString();
        
        var errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("field", error.getField());
                errorMap.put("code", "VALIDATION_ERROR");
                errorMap.put("message", error.getDefaultMessage());
                return errorMap;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "VALIDATION_ERROR");
        response.put("message", "Request validation failed");
        response.put("timestamp", Instant.now().toString());
        response.put("traceId", traceId);
        response.put("errors", errors);

        log.warn("Validation error [traceId={}]: {}", traceId, errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        String traceId = UUID.randomUUID().toString();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "BAD_REQUEST");
        response.put("message", ex.getMessage());
        response.put("timestamp", Instant.now().toString());
        response.put("traceId", traceId);

        log.warn("Bad request [traceId={}]: {}", traceId, ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "INTERNAL_ERROR");
        response.put("message", "An internal error occurred. Please contact support with trace ID.");
        response.put("timestamp", Instant.now().toString());
        response.put("traceId", traceId);

        log.error("Internal error [traceId={}]: {}", traceId, ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
