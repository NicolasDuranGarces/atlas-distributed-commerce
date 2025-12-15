package com.atlas.order.service;

import com.atlas.common.dto.ApiResponse;
import com.atlas.common.event.NotificationEvent;
import com.atlas.common.event.OrderCreatedEvent;
import com.atlas.common.exception.BusinessException;
import com.atlas.common.exception.ResourceNotFoundException;
import com.atlas.order.client.ProductClient;
import com.atlas.order.dto.*;
import com.atlas.order.entity.Order;
import com.atlas.order.entity.OrderItem;
import com.atlas.order.entity.OrderStatus;
import com.atlas.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for order operations with SAGA pattern for distributed transactions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final RabbitTemplate rabbitTemplate;

    private static final String ORDER_EXCHANGE = "order.exchange";

    /**
     * Create a new order with SAGA pattern.
     * Steps: 1. Create order, 2. Reserve inventory, 3. Process payment (async)
     */
    @Transactional
    public OrderResponse createOrder(UUID userId, String userEmail, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);

        // Generate order number
        String orderNumber = generateOrderNumber();

        // Create order entity
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .userEmail(userEmail)
                .status(OrderStatus.PENDING)
                .currency("USD")
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .shippingStreet(request.getShippingAddress().getStreet())
                .shippingCity(request.getShippingAddress().getCity())
                .shippingState(request.getShippingAddress().getState())
                .shippingPostalCode(request.getShippingAddress().getPostalCode())
                .shippingCountry(request.getShippingAddress().getCountry())
                .recipientName(request.getShippingAddress().getRecipientName())
                .recipientPhone(request.getShippingAddress().getRecipientPhone())
                .build();

        // Process order items
        List<ReservedItem> reservedItems = new ArrayList<>();
        try {
            for (OrderItemRequest itemRequest : request.getItems()) {
                // Get product info
                ApiResponse<ProductClient.ProductInfo> productResponse = 
                        productClient.getProduct(itemRequest.getProductId());
                
                if (!productResponse.isSuccess() || productResponse.getData() == null) {
                    throw new BusinessException("Product not found: " + itemRequest.getProductId());
                }

                ProductClient.ProductInfo product = productResponse.getData();

                // Check availability
                if (!product.inStock() || product.availableQuantity() < itemRequest.getQuantity()) {
                    throw new BusinessException(
                            "Insufficient stock for product: " + product.name());
                }

                // Reserve inventory
                ApiResponse<Void> reserveResponse = productClient.reserveInventory(
                        product.id(), itemRequest.getQuantity(), order.getId());

                if (!reserveResponse.isSuccess()) {
                    throw new BusinessException("Failed to reserve inventory for: " + product.name());
                }

                reservedItems.add(new ReservedItem(product.id(), itemRequest.getQuantity()));

                // Create order item
                OrderItem orderItem = OrderItem.builder()
                        .productId(product.id())
                        .productSku(product.sku())
                        .productName(product.name())
                        .productImageUrl(product.imageUrl())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(product.price())
                        .subtotal(product.price().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                        .build();

                order.addItem(orderItem);
            }

            // Calculate totals
            order.calculateTotals();
            order = orderRepository.save(order);

            // Publish order created event
            publishOrderCreatedEvent(order);

            log.info("Order created successfully: {}", order.getOrderNumber());
            return mapToResponse(order);

        } catch (Exception e) {
            // SAGA compensation: release reserved inventory
            log.error("Order creation failed, compensating: {}", e.getMessage());
            compensateInventory(reservedItems, order.getId());
            throw e;
        }
    }

    /**
     * Get order by ID.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Verify ownership
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("Order does not belong to user", "FORBIDDEN");
        }

        return mapToResponse(order);
    }

    /**
     * Get orders for user.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(UUID userId, int page, int size) {
        return orderRepository.findByUserId(userId, PageRequest.of(page, size))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel order - SAGA compensation.
     */
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("Order does not belong to user", "FORBIDDEN");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || 
            order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel shipped or delivered order");
        }

        // Release inventory
        for (OrderItem item : order.getItems()) {
            try {
                productClient.releaseInventory(item.getProductId(), item.getQuantity(), orderId);
            } catch (Exception e) {
                log.error("Failed to release inventory for product {}: {}", 
                        item.getProductId(), e.getMessage());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        // Publish cancellation event
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE, "order.cancelled", 
                Map.of("orderId", orderId, "userId", userId));

        return mapToResponse(order);
    }

    /**
     * Update order status (internal use).
     */
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        order.setStatus(status);
        
        if (status == OrderStatus.CONFIRMED) {
            order.setPaidAt(LocalDateTime.now());
        } else if (status == OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        } else if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        
        orderRepository.save(order);
    }

    private void compensateInventory(List<ReservedItem> reservedItems, UUID orderId) {
        for (ReservedItem item : reservedItems) {
            try {
                productClient.releaseInventory(item.productId, item.quantity, orderId);
            } catch (Exception e) {
                log.error("Compensation failed for product {}: {}", item.productId, e.getMessage());
            }
        }
    }

    private void publishOrderCreatedEvent(Order order) {
        List<OrderCreatedEvent.OrderItem> eventItems = order.getItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()))
                .collect(Collectors.toList());

        OrderCreatedEvent event = OrderCreatedEvent.create(
                order.getId(),
                order.getUserId(),
                eventItems,
                order.getTotalAmount(),
                order.getShippingAddress()
        );

        rabbitTemplate.convertAndSend(ORDER_EXCHANGE, "order.created", event);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD-" + timestamp + "-" + random;
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productSku(item.getProductSku())
                        .productName(item.getProductName())
                        .productImageUrl(item.getProductImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .items(items)
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .shippingAddress(order.getShippingAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .paymentMethod(order.getPaymentMethod())
                .trackingNumber(order.getTrackingNumber())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }

    private record ReservedItem(UUID productId, int quantity) {}
}
