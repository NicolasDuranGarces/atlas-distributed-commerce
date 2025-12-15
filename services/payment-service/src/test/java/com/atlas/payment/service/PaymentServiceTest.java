package com.atlas.payment.service;

import com.atlas.common.exception.BusinessException;
import com.atlas.common.exception.ResourceNotFoundException;
import com.atlas.payment.dto.PaymentResponse;
import com.atlas.payment.dto.ProcessPaymentRequest;
import com.atlas.payment.entity.Payment;
import com.atlas.payment.entity.PaymentMethod;
import com.atlas.payment.entity.PaymentStatus;
import com.atlas.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private Payment testPayment;
    private UUID userId;
    private ProcessPaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testPayment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .userId(userId)
                .idempotencyKey("idem-key-123")
                .amount(new BigDecimal("199.99"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .transactionId("TXN-123")
                .cardLastFour("4242")
                .cardBrand("VISA")
                .processedAt(LocalDateTime.now())
                .build();

        paymentRequest = ProcessPaymentRequest.builder()
                .orderId(UUID.randomUUID())
                .idempotencyKey("new-idem-key")
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .cardLastFour("1234")
                .cardBrand("MASTERCARD")
                .build();
    }

    @Test
    @DisplayName("Should return existing payment for duplicate idempotency key")
    void processPayment_IdempotencyHit_ReturnsExisting() {
        when(paymentRepository.existsByIdempotencyKey(anyString())).thenReturn(true);
        when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(testPayment));

        PaymentResponse response = paymentService.processPayment(userId, paymentRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo("TXN-123");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should get payment by ID")
    void getPayment_Success() {
        when(paymentRepository.findById(testPayment.getId())).thenReturn(Optional.of(testPayment));

        PaymentResponse response = paymentService.getPayment(testPayment.getId(), userId);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("199.99"));
    }

    @Test
    @DisplayName("Should throw exception when payment not found")
    void getPayment_NotFound_ThrowsException() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(paymentId, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when payment belongs to different user")
    void getPayment_WrongUser_ThrowsException() {
        UUID differentUserId = UUID.randomUUID();
        when(paymentRepository.findById(testPayment.getId())).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.getPayment(testPayment.getId(), differentUserId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not belong to user");
    }

    @Test
    @DisplayName("Should get payment by order ID")
    void getPaymentByOrder_Success() {
        when(paymentRepository.findByOrderId(testPayment.getOrderId()))
                .thenReturn(Optional.of(testPayment));

        PaymentResponse response = paymentService.getPaymentByOrder(testPayment.getOrderId());

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(testPayment.getOrderId());
    }

    @Test
    @DisplayName("Should refund payment successfully")
    void refundPayment_Success() {
        when(paymentRepository.findById(testPayment.getId())).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse response = paymentService.refundPayment(testPayment.getId(), userId);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED.name());
    }

    @Test
    @DisplayName("Should not refund non-completed payment")
    void refundPayment_NotCompleted_ThrowsException() {
        testPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(testPayment.getId())).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.refundPayment(testPayment.getId(), userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Can only refund completed payments");
    }
}
