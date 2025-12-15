package com.atlas.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.UUID;

/**
 * Event published when a notification needs to be sent.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends BaseEvent {

    private UUID userId;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationType type;
    private String templateName;
    private String subject;
    private Map<String, Object> templateData;
    private NotificationChannel channel;
    private Priority priority;

    public enum NotificationType {
        ORDER_CONFIRMATION,
        ORDER_SHIPPED,
        ORDER_DELIVERED,
        ORDER_CANCELLED,
        PAYMENT_RECEIVED,
        PAYMENT_FAILED,
        PASSWORD_RESET,
        WELCOME,
        PROMOTIONAL
    }

    public enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH,
        ALL
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }

    public static NotificationEvent orderConfirmation(UUID userId, String email,
            Map<String, Object> orderData) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(userId)
                .recipientEmail(email)
                .type(NotificationType.ORDER_CONFIRMATION)
                .templateName("order-confirmation")
                .subject("Order Confirmation - Atlas Commerce")
                .templateData(orderData)
                .channel(NotificationChannel.EMAIL)
                .priority(Priority.HIGH)
                .build();
        event.initializeEvent("NOTIFICATION_REQUESTED", userId, "order-service");
        return event;
    }

    public static NotificationEvent paymentReceived(UUID userId, String email,
            Map<String, Object> paymentData) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(userId)
                .recipientEmail(email)
                .type(NotificationType.PAYMENT_RECEIVED)
                .templateName("payment-received")
                .subject("Payment Confirmed - Atlas Commerce")
                .templateData(paymentData)
                .channel(NotificationChannel.EMAIL)
                .priority(Priority.NORMAL)
                .build();
        event.initializeEvent("NOTIFICATION_REQUESTED", userId, "payment-service");
        return event;
    }
}
