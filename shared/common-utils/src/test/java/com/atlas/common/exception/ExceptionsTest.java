package com.atlas.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ExceptionsTest {

    @Test
    @DisplayName("ResourceNotFoundException should format message correctly")
    void resourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", "123");

        assertThat(ex.getMessage()).isEqualTo("User not found with id: '123'");
        assertThat(ex.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    @DisplayName("ResourceNotFoundException with simple message")
    void resourceNotFoundException_SimpleMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        assertThat(ex.getMessage()).isEqualTo("Resource not found");
    }

    @Test
    @DisplayName("BusinessException should have error code")
    void businessException() {
        BusinessException ex = new BusinessException("Invalid operation", "INVALID_OPERATION");

        assertThat(ex.getMessage()).isEqualTo("Invalid operation");
        assertThat(ex.getErrorCode()).isEqualTo("INVALID_OPERATION");
    }

    @Test
    @DisplayName("BusinessException should have default error code")
    void businessException_DefaultCode() {
        BusinessException ex = new BusinessException("Invalid operation");

        assertThat(ex.getMessage()).isEqualTo("Invalid operation");
        assertThat(ex.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
    }

    @Test
    @DisplayName("AuthenticationException should have message")
    void authenticationException() {
        AuthenticationException ex = new AuthenticationException("Invalid credentials");

        assertThat(ex.getMessage()).isEqualTo("Invalid credentials");
        assertThat(ex.getErrorCode()).isEqualTo("AUTHENTICATION_FAILED");
    }

    @Test
    @DisplayName("InsufficientStockException should format message correctly")
    void insufficientStockException() {
        InsufficientStockException ex = new InsufficientStockException("PROD-001", 10, 5);

        assertThat(ex.getMessage()).contains("PROD-001");
        assertThat(ex.getMessage()).contains("10");
        assertThat(ex.getMessage()).contains("5");
        assertThat(ex.getErrorCode()).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    @DisplayName("PaymentException should have message and cause")
    void paymentException() {
        Exception cause = new RuntimeException("Gateway error");
        PaymentException ex = new PaymentException("Payment failed", cause);

        assertThat(ex.getMessage()).isEqualTo("Payment failed");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("PaymentException with simple message")
    void paymentException_SimpleMessage() {
        PaymentException ex = new PaymentException("Card declined");

        assertThat(ex.getMessage()).isEqualTo("Card declined");
        assertThat(ex.getErrorCode()).isEqualTo("PAYMENT_FAILED");
    }
}
