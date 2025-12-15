package com.atlas.order.entity;

/**
 * Order status enumeration representing the order lifecycle.
 */
public enum OrderStatus {
    PENDING,            // Order created, awaiting payment
    PAYMENT_PROCESSING, // Payment being processed
    PAYMENT_FAILED,     // Payment failed
    CONFIRMED,          // Payment successful, order confirmed
    PROCESSING,         // Order being prepared
    SHIPPED,            // Order shipped
    DELIVERED,          // Order delivered
    CANCELLED,          // Order cancelled
    REFUNDED            // Order refunded
}
