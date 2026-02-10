package com.transaction.common.exception;

import com.transaction.common.model.ErrorResponse;
import com.transaction.common.model.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST APIs
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ValidationError.of(
                        error.getField(),
                        error.getCode(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.error(
                "VALIDATION_ERROR",
                "Validation failed",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.error(
                "NOT_FOUND",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle business exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("Business error: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.error(
                ex.getCode(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.error(
                "INTERNAL_ERROR",
                "An unexpected error occurred"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
