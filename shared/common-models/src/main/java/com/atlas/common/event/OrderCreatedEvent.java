package com.atlas.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Event published when a new order is created.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {

    private UUID orderId;
    private UUID userId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private UUID productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    public static OrderCreatedEvent create(UUID orderId, UUID userId, List<OrderItem> items,
            BigDecimal totalAmount, String shippingAddress) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .items(items)
                .totalAmount(totalAmount)
                .shippingAddress(shippingAddress)
                .status("PENDING")
                .build();
        event.initializeEvent("ORDER_CREATED", orderId, "order-service");
        return event;
    }
}
