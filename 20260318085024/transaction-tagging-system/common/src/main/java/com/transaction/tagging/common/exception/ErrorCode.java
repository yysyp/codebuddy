package com.transaction.tagging.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of error codes used throughout the application.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Generic errors
    SUCCESS("SUCCESS", "Operation completed successfully", 200),
    UNKNOWN_ERROR("UNKNOWN_ERROR", "An unexpected error occurred", 500),
    INVALID_REQUEST("INVALID_REQUEST", "Invalid request parameters", 400),
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed", 400),

    // Resource errors
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource not found", 404),
    RESOURCE_ALREADY_EXISTS("RESOURCE_ALREADY_EXISTS", "Resource already exists", 409),
    RESOURCE_CONFLICT("RESOURCE_CONFLICT", "Resource conflict", 409),

    // Transaction errors
    TRANSACTION_NOT_FOUND("TRANSACTION_NOT_FOUND", "Transaction not found", 404),
    TRANSACTION_INVALID_STATUS("TRANSACTION_INVALID_STATUS", "Invalid transaction status", 400),

    // Rule errors
    RULE_NOT_FOUND("RULE_NOT_FOUND", "Rule not found", 404),
    RULE_ALREADY_EXISTS("RULE_ALREADY_EXISTS", "Rule already exists", 409),
    RULE_COMPILATION_ERROR("RULE_COMPILATION_ERROR", "Failed to compile rule", 400),
    RULE_EXECUTION_ERROR("RULE_EXECUTION_ERROR", "Failed to execute rule", 500),
    RULE_INVALID_STATUS("RULE_INVALID_STATUS", "Invalid rule status transition", 400),
    RULE_VERSION_CONFLICT("RULE_VERSION_CONFLICT", "Rule version conflict", 409),

    // Tag errors
    TAG_NOT_FOUND("TAG_NOT_FOUND", "Tag not found", 404),
    TAG_ALREADY_EXISTS("TAG_ALREADY_EXISTS", "Tag already exists", 409),
    TAG_INVALID_CATEGORY("TAG_INVALID_CATEGORY", "Invalid tag category", 400),

    // Schema errors
    SCHEMA_NOT_FOUND("SCHEMA_NOT_FOUND", "Schema not found", 404),
    SCHEMA_VALIDATION_ERROR("SCHEMA_VALIDATION_ERROR", "Schema validation failed", 400),

    // Flink errors
    FLINK_JOB_ERROR("FLINK_JOB_ERROR", "Flink job execution error", 500),
    FLINK_CONNECTION_ERROR("FLINK_CONNECTION_ERROR", "Failed to connect to Flink cluster", 500),

    // Authorization errors
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized access", 401),
    FORBIDDEN("FORBIDDEN", "Access denied", 403);

    private final String code;
    private final String message;
    private final int httpStatus;

    /**
     * Create a BusinessException from this error code
     */
    public BusinessException toException() {
        return new BusinessException(code, message, httpStatus);
    }

    /**
     * Create a BusinessException with custom message
     */
    public BusinessException toException(String customMessage) {
        return new BusinessException(code, customMessage, httpStatus);
    }

    /**
     * Create a BusinessException with cause
     */
    public BusinessException toException(Throwable cause) {
        return new BusinessException(code, message, httpStatus, cause);
    }
}
