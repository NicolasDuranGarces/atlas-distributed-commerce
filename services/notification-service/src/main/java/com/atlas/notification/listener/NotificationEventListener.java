package com.atlas.notification.listener;

import com.atlas.common.event.NotificationEvent;
import com.atlas.common.event.OrderCreatedEvent;
import com.atlas.common.event.PaymentProcessedEvent;
import com.atlas.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Event listener for processing notification events from other services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order created event: {}", event.getOrderId());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("orderId", event.getOrderId().toString());
        templateData.put("orderNumber", event.getAggregateId());
        templateData.put("items", event.getItems());
        templateData.put("totalAmount", event.getTotalAmount());
        templateData.put("shippingAddress", event.getShippingAddress());

        // Note: In production, we would fetch user email from User Service
        // For demo, this would be triggered with user email from the event
        log.info("Order confirmation notification queued for order: {}", event.getOrderId());
    }

    @RabbitListener(queues = "payment.completed.queue")
    public void handlePaymentCompleted(PaymentProcessedEvent event) {
        log.info("Received payment completed event: {}", event.getPaymentId());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("paymentId", event.getPaymentId().toString());
        templateData.put("orderId", event.getOrderId().toString());
        templateData.put("amount", event.getAmount());
        templateData.put("transactionId", event.getTransactionId());

        log.info("Payment confirmation notification queued for payment: {}", event.getPaymentId());
    }

    @RabbitListener(queues = "payment.failed.queue")
    public void handlePaymentFailed(PaymentProcessedEvent event) {
        log.warn("Received payment failed event: {}", event.getPaymentId());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("paymentId", event.getPaymentId().toString());
        templateData.put("orderId", event.getOrderId().toString());
        templateData.put("amount", event.getAmount());
        templateData.put("failureReason", event.getFailureReason());

        log.info("Payment failure notification queued for payment: {}", event.getPaymentId());
    }

    @RabbitListener(queues = "notification.queue")
    public void handleNotificationRequest(NotificationEvent event) {
        log.info("Received notification request: {} for user {}", 
                event.getType(), event.getUserId());

        switch (event.getChannel()) {
            case EMAIL -> emailService.sendEmail(
                    event.getRecipientEmail(),
                    event.getSubject(),
                    event.getTemplateName(),
                    event.getTemplateData()
            );
            case SMS -> log.info("SMS notification would be sent to: {}", event.getRecipientPhone());
            case ALL -> {
                emailService.sendEmail(
                        event.getRecipientEmail(),
                        event.getSubject(),
                        event.getTemplateName(),
                        event.getTemplateData()
                );
                log.info("SMS notification would be sent to: {}", event.getRecipientPhone());
            }
            default -> log.warn("Unknown notification channel: {}", event.getChannel());
        }
    }
}
