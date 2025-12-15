package com.atlas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessException extends AtlasException {

    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);
    }
}
