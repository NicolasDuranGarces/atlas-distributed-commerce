package com.atlas.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback controller for circuit breaker responses.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return fallbackResponse("User service is temporarily unavailable");
    }

    @GetMapping("/product")
    public ResponseEntity<Map<String, Object>> productServiceFallback() {
        return fallbackResponse("Product service is temporarily unavailable");
    }

    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> orderServiceFallback() {
        return fallbackResponse("Order service is temporarily unavailable");
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return fallbackResponse("Payment service is temporarily unavailable");
    }

    private ResponseEntity<Map<String, Object>> fallbackResponse(String message) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", message,
                        "error", Map.of(
                                "code", "SERVICE_UNAVAILABLE",
                                "message", message
                        ),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }
}
