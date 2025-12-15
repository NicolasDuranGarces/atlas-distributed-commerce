package com.atlas.order.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("Should create order with builder")
    void createOrder_WithBuilder() {
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-001")
                .userId(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("199.99"))
                .build();

        assertThat(order.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should add item to order")
    void addItem() {
        Order order = Order.builder()
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .productId(UUID.randomUUID())
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();

        order.addItem(item);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("Should calculate totals correctly")
    void calculateTotals() {
        Order order = Order.builder()
                .items(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .productId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();

        OrderItem item2 = OrderItem.builder()
                .productId(UUID.randomUUID())
                .quantity(1)
                .unitPrice(new BigDecimal("75.00"))
                .subtotal(new BigDecimal("75.00"))
                .build();

        order.addItem(item1);
        order.addItem(item2);
        order.calculateTotals();

        assertThat(order.getSubtotal()).isEqualTo(new BigDecimal("175.00"));
        assertThat(order.getTaxAmount()).isEqualByComparingTo(new BigDecimal("14.00")); // 8%
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("189.00"));
    }

    @Test
    @DisplayName("Should get total items count")
    void getTotalItems() {
        Order order = Order.builder()
                .items(new ArrayList<>())
                .build();

        order.addItem(OrderItem.builder().quantity(2).build());
        order.addItem(OrderItem.builder().quantity(3).build());

        assertThat(order.getTotalItems()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should get shipping address")
    void getShippingAddress() {
        Order order = Order.builder()
                .shippingStreet("123 Main St")
                .shippingCity("City")
                .shippingState("State")
                .shippingPostalCode("12345")
                .shippingCountry("USA")
                .build();

        assertThat(order.getShippingAddress()).isEqualTo("123 Main St, City, State 12345, USA");
    }

    @Test
    @DisplayName("Default values should be set correctly")
    void defaultValues() {
        Order order = Order.builder()
                .userId(UUID.randomUUID())
                .build();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getCurrency()).isEqualTo("USD");
    }
}
