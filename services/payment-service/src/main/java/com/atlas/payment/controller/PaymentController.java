package com.atlas.payment.controller;

import com.atlas.common.dto.ApiResponse;
import com.atlas.payment.dto.*;
import com.atlas.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        
        PaymentResponse response = paymentService.processPayment(
                UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID paymentId,
            @RequestHeader("X-User-Id") String userId) {
        
        PaymentResponse response = paymentService.getPayment(
                paymentId, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(
            @PathVariable UUID orderId) {
        PaymentResponse response = paymentService.getPaymentByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable UUID paymentId,
            @RequestHeader("X-User-Id") String userId) {
        
        PaymentResponse response = paymentService.refundPayment(
                paymentId, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed"));
    }
}
