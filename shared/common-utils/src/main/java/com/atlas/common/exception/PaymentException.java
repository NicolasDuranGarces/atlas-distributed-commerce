package com.atlas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a payment operation fails.
 */
public class PaymentException extends AtlasException {

    public PaymentException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED, "PAYMENT_FAILED");
    }

    public PaymentException(String message, String errorCode) {
        super(message, HttpStatus.PAYMENT_REQUIRED, errorCode);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause, HttpStatus.PAYMENT_REQUIRED, "PAYMENT_FAILED");
    }
}
