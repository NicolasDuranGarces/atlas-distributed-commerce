package com.atlas.payment.service;

import com.atlas.common.event.PaymentProcessedEvent;
import com.atlas.common.exception.BusinessException;
import com.atlas.common.exception.PaymentException;
import com.atlas.common.exception.ResourceNotFoundException;
import com.atlas.payment.dto.*;
import com.atlas.payment.entity.Payment;
import com.atlas.payment.entity.PaymentMethod;
import com.atlas.payment.entity.PaymentStatus;
import com.atlas.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Service for payment processing with simulated gateway.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String PAYMENT_EXCHANGE = "payment.exchange";

    /**
     * Process a payment with idempotency.
     */
    @Transactional
    public PaymentResponse processPayment(UUID userId, ProcessPaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Check idempotency - return existing payment if already processed
        if (paymentRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            Payment existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow();
            log.info("Returning existing payment for idempotency key: {}", request.getIdempotencyKey());
            return mapToResponse(existing);
        }

        // Create payment record
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .idempotencyKey(request.getIdempotencyKey())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
                .status(PaymentStatus.PROCESSING)
                .cardLastFour(request.getCardLastFour())
                .cardBrand(request.getCardBrand())
                .build();

        payment = paymentRepository.save(payment);

        // Simulate payment processing
        try {
            SimulatedResult result = simulatePaymentGateway(request);

            if (result.success) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(result.transactionId);
                payment.setGatewayResponse(result.response);
                payment.setProcessedAt(LocalDateTime.now());

                // Publish success event
                PaymentProcessedEvent event = PaymentProcessedEvent.success(
                        payment.getId(), payment.getOrderId(), userId,
                        payment.getAmount(), result.transactionId);
                rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, "payment.completed", event);

                log.info("Payment completed: {}", payment.getTransactionId());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason(result.error);
                payment.setGatewayResponse(result.response);

                // Publish failure event
                PaymentProcessedEvent event = PaymentProcessedEvent.failure(
                        payment.getId(), payment.getOrderId(), userId,
                        payment.getAmount(), result.error);
                rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, "payment.failed", event);

                log.warn("Payment failed: {}", result.error);
            }

            payment = paymentRepository.save(payment);
            return mapToResponse(payment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Processing error: " + e.getMessage());
            paymentRepository.save(payment);
            throw new PaymentException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get payment by ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException("Payment does not belong to user", "FORBIDDEN");
        }

        return mapToResponse(payment);
    }

    /**
     * Get payment by order ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToResponse(payment);
    }

    /**
     * Process refund.
     */
    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException("Payment does not belong to user", "FORBIDDEN");
        }

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("Can only refund completed payments");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundAmount(payment.getAmount());

        payment = paymentRepository.save(payment);

        // Publish refund event
        rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, "payment.refunded",
                java.util.Map.of(
                        "paymentId", paymentId,
                        "orderId", payment.getOrderId(),
                        "amount", payment.getAmount()
                ));

        return mapToResponse(payment);
    }

    /**
     * Simulated payment gateway (for demo purposes).
     */
    private SimulatedResult simulatePaymentGateway(ProcessPaymentRequest request) {
        // Simulate processing delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 95% success rate simulation
        Random random = new Random();
        if (random.nextInt(100) < 95) {
            String transactionId = "TXN-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
            return new SimulatedResult(true, transactionId, "{\"status\":\"approved\"}", null);
        } else {
            return new SimulatedResult(false, null, "{\"status\":\"declined\"}", "Card declined");
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .cardLastFour(payment.getCardLastFour())
                .cardBrand(payment.getCardBrand())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .refundedAt(payment.getRefundedAt())
                .build();
    }

    private record SimulatedResult(boolean success, String transactionId, String response, String error) {}
}
