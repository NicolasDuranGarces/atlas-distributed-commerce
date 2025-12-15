package com.atlas.common.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class EventsTest {

    @Test
    @DisplayName("Should create order created event with factory method")
    void orderCreatedEvent_Create() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<OrderCreatedEvent.OrderItem> items = List.of(
                new OrderCreatedEvent.OrderItem(UUID.randomUUID(), "Product 1", 2,
                        new BigDecimal("50.00"), new BigDecimal("100.00")));

        OrderCreatedEvent event = OrderCreatedEvent.create(
                orderId, userId, items, new BigDecimal("100.00"), "123 Main St");

        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getItems()).hasSize(1);
        assertThat(event.getTotalAmount()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should create success payment event")
    void paymentProcessedEvent_Success() {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentProcessedEvent event = PaymentProcessedEvent.success(
                paymentId, orderId, userId, new BigDecimal("100.00"), "TXN-123");

        assertThat(event.getPaymentId()).isEqualTo(paymentId);
        assertThat(event.getTransactionId()).isEqualTo("TXN-123");
        assertThat(event.getStatus()).isEqualTo(PaymentProcessedEvent.PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should create failure payment event")
    void paymentProcessedEvent_Failure() {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentProcessedEvent event = PaymentProcessedEvent.failure(
                paymentId, orderId, userId, new BigDecimal("100.00"), "Card declined");

        assertThat(event.getPaymentId()).isEqualTo(paymentId);
        assertThat(event.getStatus()).isEqualTo(PaymentProcessedEvent.PaymentStatus.FAILED);
        assertThat(event.getFailureReason()).isEqualTo("Card declined");
    }

    @Test
    @DisplayName("Should create inventory updated event with builder")
    void inventoryUpdatedEvent_Builder() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        InventoryUpdatedEvent event = InventoryUpdatedEvent.builder()
                .productId(productId)
                .quantityChanged(-5)
                .newQuantity(95)
                .orderId(orderId)
                .build();

        assertThat(event.getProductId()).isEqualTo(productId);
        assertThat(event.getQuantityChanged()).isEqualTo(-5);
        assertThat(event.getNewQuantity()).isEqualTo(95);
    }

    @Test
    @DisplayName("Should create notification event with factory method")
    void notificationEvent_OrderConfirmation() {
        UUID userId = UUID.randomUUID();
        Map<String, Object> orderData = Map.of("orderId", "123", "total", "100.00");

        NotificationEvent event = NotificationEvent.orderConfirmation(userId, "test@example.com", orderData);

        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getRecipientEmail()).isEqualTo("test@example.com");
        assertThat(event.getChannel()).isEqualTo(NotificationEvent.NotificationChannel.EMAIL);
        assertThat(event.getType()).isEqualTo(NotificationEvent.NotificationType.ORDER_CONFIRMATION);
    }

    @Test
    @DisplayName("Should create payment received notification")
    void notificationEvent_PaymentReceived() {
        UUID userId = UUID.randomUUID();
        Map<String, Object> paymentData = Map.of("amount", "100.00");

        NotificationEvent event = NotificationEvent.paymentReceived(userId, "test@example.com", paymentData);

        assertThat(event.getType()).isEqualTo(NotificationEvent.NotificationType.PAYMENT_RECEIVED);
        assertThat(event.getChannel()).isEqualTo(NotificationEvent.NotificationChannel.EMAIL);
    }

    @Test
    @DisplayName("Notification channel enum values")
    void notificationChannel_Values() {
        assertThat(NotificationEvent.NotificationChannel.values()).contains(
                NotificationEvent.NotificationChannel.EMAIL,
                NotificationEvent.NotificationChannel.SMS,
                NotificationEvent.NotificationChannel.PUSH,
                NotificationEvent.NotificationChannel.ALL);
    }

    @Test
    @DisplayName("Payment status enum values")
    void paymentStatus_Values() {
        assertThat(PaymentProcessedEvent.PaymentStatus.values()).contains(
                PaymentProcessedEvent.PaymentStatus.PENDING,
                PaymentProcessedEvent.PaymentStatus.COMPLETED,
                PaymentProcessedEvent.PaymentStatus.FAILED,
                PaymentProcessedEvent.PaymentStatus.REFUNDED);
    }
}
