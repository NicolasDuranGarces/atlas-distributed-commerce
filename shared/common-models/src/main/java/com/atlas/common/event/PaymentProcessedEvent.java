package com.atlas.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when a payment is processed.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentProcessedEvent extends BaseEvent {

    private UUID paymentId;
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED,
        CANCELLED
    }

    public static PaymentProcessedEvent success(UUID paymentId, UUID orderId, UUID userId,
            BigDecimal amount, String transactionId) {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .transactionId(transactionId)
                .build();
        event.initializeEvent("PAYMENT_COMPLETED", paymentId, "payment-service");
        return event;
    }

    public static PaymentProcessedEvent failure(UUID paymentId, UUID orderId, UUID userId,
            BigDecimal amount, String reason) {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .currency("USD")
                .status(PaymentStatus.FAILED)
                .failureReason(reason)
                .build();
        event.initializeEvent("PAYMENT_FAILED", paymentId, "payment-service");
        return event;
    }
}
