package com.example.drools.common.exception;

import com.example.drools.common.response.ApiResponse;
import com.example.drools.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for all controllers
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        List<ErrorResponse> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(ErrorResponse.builder()
                    .field(error.getField())
                    .code("VALIDATION_ERROR")
                    .message(error.getDefaultMessage())
                    .build());
        }

        log.error("Validation error: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("400", "Validation failed", errors));
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        List<ErrorResponse> errors = List.of(
                ErrorResponse.builder()
                        .code("ILLEGAL_ARGUMENT")
                        .message(ex.getMessage())
                        .build()
        );

        log.error("Illegal argument error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("400", "Invalid argument", errors));
    }

    /**
     * Handle rule engine exceptions
     */
    @ExceptionHandler(RuleEngineException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuleEngineException(
            RuleEngineException ex,
            WebRequest request) {

        List<ErrorResponse> errors = List.of(
                ErrorResponse.builder()
                        .code("RULE_ENGINE_ERROR")
                        .message(ex.getMessage())
                        .build()
        );

        log.error("Rule engine error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("500", "Rule engine error", errors));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(
            Exception ex,
            WebRequest request) {

        List<ErrorResponse> errors = List.of(
                ErrorResponse.builder()
                        .code("INTERNAL_ERROR")
                        .message("An unexpected error occurred")
                        .build()
        );

        log.error("Unexpected error: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("500", "Internal server error", errors));
    }
}
