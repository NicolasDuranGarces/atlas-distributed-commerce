package com.atlas.payment.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Test
    @DisplayName("Should create payment with builder")
    void createPayment_WithBuilder() {
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .userId(UUID.randomUUID())
                .idempotencyKey("idem-key-123")
                .amount(new BigDecimal("199.99"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualTo(new BigDecimal("199.99"));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Default values should be set correctly")
    void defaultValues() {
        Payment payment = Payment.builder()
                .orderId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .idempotencyKey("key")
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        assertThat(payment.getCurrency()).isEqualTo("USD");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("PaymentMethod enum values")
    void paymentMethodValues() {
        assertThat(PaymentMethod.values()).contains(
                PaymentMethod.CREDIT_CARD,
                PaymentMethod.DEBIT_CARD,
                PaymentMethod.PAYPAL,
                PaymentMethod.BANK_TRANSFER,
                PaymentMethod.CRYPTO,
                PaymentMethod.SIMULATED
        );
    }

    @Test
    @DisplayName("PaymentStatus enum values")
    void paymentStatusValues() {
        assertThat(PaymentStatus.values()).contains(
                PaymentStatus.PENDING,
                PaymentStatus.PROCESSING,
                PaymentStatus.COMPLETED,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.REFUNDED,
                PaymentStatus.PARTIALLY_REFUNDED
        );
    }
}
