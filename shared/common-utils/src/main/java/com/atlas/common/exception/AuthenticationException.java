package com.atlas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for authentication failures.
 */
public class AuthenticationException extends AtlasException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }

    public AuthenticationException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }
}
