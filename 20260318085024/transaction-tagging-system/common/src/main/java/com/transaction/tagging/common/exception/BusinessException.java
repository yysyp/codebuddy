package com.transaction.tagging.common.exception;

import lombok.Getter;

/**
 * Base business exception for the application.
 * Used to distinguish business errors from system errors.
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Error code for client identification
     */
    private final String code;

    /**
     * HTTP status code to return
     */
    private final int httpStatus;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public BusinessException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = 400;
    }

    public BusinessException(String code, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
