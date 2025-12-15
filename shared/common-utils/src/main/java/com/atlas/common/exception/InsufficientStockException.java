package com.atlas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when insufficient stock is available.
 */
public class InsufficientStockException extends AtlasException {

    public InsufficientStockException(String productName, int requested, int available) {
        super(
                String.format("Insufficient stock for '%s'. Requested: %d, Available: %d",
                        productName, requested, available),
                HttpStatus.CONFLICT,
                "INSUFFICIENT_STOCK");
    }

    public InsufficientStockException(String message) {
        super(message, HttpStatus.CONFLICT, "INSUFFICIENT_STOCK");
    }
}
