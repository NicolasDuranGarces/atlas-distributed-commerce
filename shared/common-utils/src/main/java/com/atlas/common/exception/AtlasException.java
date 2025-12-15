package com.atlas.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all Atlas application exceptions.
 * Provides HTTP status and error code for consistent error handling.
 */
@Getter
public abstract class AtlasException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected AtlasException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    protected AtlasException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
