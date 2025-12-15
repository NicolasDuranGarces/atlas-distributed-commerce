package com.atlas.order.service;

import com.atlas.common.dto.ApiResponse;
import com.atlas.common.exception.BusinessException;
import com.atlas.common.exception.ResourceNotFoundException;
import com.atlas.order.client.ProductClient;
import com.atlas.order.dto.*;
import com.atlas.order.entity.Order;
import com.atlas.order.entity.OrderItem;
import com.atlas.order.entity.OrderStatus;
import com.atlas.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ProductClient productClient;
    
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private UUID userId;
    private ProductClient.ProductInfo productInfo;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testOrder = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-20241214-0001")
                .userId(userId)
                .userEmail("test@example.com")
                .status(OrderStatus.PENDING)
                .subtotal(new BigDecimal("199.98"))
                .taxAmount(new BigDecimal("20.00"))
                .shippingAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("229.98"))
                .currency("USD")
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productSku("PROD-001")
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .subtotal(new BigDecimal("199.98"))
                .build();
        testOrder.getItems().add(item);

        productInfo = new ProductClient.ProductInfo(
                UUID.randomUUID(),
                "PROD-001",
                "Test Product",
                new BigDecimal("99.99"),
                "http://example.com/image.jpg",
                100,
                true
        );
    }

    @Test
    @DisplayName("Should create order successfully")
    void createOrder_Success() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(productInfo.id());
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));

        ShippingAddressRequest address = new ShippingAddressRequest();
        address.setStreet("123 Main St");
        address.setCity("City");
        address.setState("State");
        address.setPostalCode("12345");
        address.setCountry("USA");
        address.setRecipientName("John Doe");
        request.setShippingAddress(address);
        request.setPaymentMethod("CREDIT_CARD");

        when(productClient.getProduct(any(UUID.class)))
                .thenReturn(ApiResponse.success(productInfo));
        when(productClient.reserveInventory(any(UUID.class), anyInt(), any(UUID.class)))
                .thenReturn(ApiResponse.success(null));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        // When
        OrderResponse response = orderService.createOrder(userId, "test@example.com", request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(productClient).reserveInventory(any(UUID.class), eq(2), any(UUID.class));
        verify(orderRepository).save(any(Order.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void createOrder_ProductNotFound_ThrowsException() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(UUID.randomUUID());
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));

        ShippingAddressRequest address = new ShippingAddressRequest();
        address.setStreet("123 Main St");
        address.setCity("City");
        address.setState("State");
        address.setPostalCode("12345");
        address.setCountry("USA");
        request.setShippingAddress(address);

        when(productClient.getProduct(any(UUID.class)))
                .thenReturn(ApiResponse.error("Not found", "NOT_FOUND"));

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(userId, "test@example.com", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Should get order by ID")
    void getOrder_Success() {
        when(orderRepository.findByIdWithItems(testOrder.getId())).thenReturn(Optional.of(testOrder));

        OrderResponse response = orderService.getOrder(testOrder.getId(), userId);

        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD-20241214-0001");
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void getOrder_NotFound_ThrowsException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(orderId, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when order belongs to different user")
    void getOrder_WrongUser_ThrowsException() {
        UUID differentUserId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(testOrder.getId())).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.getOrder(testOrder.getId(), differentUserId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not belong to user");
    }

    @Test
    @DisplayName("Should get user orders")
    void getUserOrders_Success() {
        when(orderRepository.findByUserId(eq(userId), any(PageRequest.class)))
                .thenReturn(List.of(testOrder));

        List<OrderResponse> orders = orderService.getUserOrders(userId, 0, 10);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderNumber()).isEqualTo("ORD-20241214-0001");
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void cancelOrder_Success() {
        when(orderRepository.findByIdWithItems(testOrder.getId())).thenReturn(Optional.of(testOrder));
        when(productClient.releaseInventory(any(UUID.class), anyInt(), any(UUID.class)))
                .thenReturn(ApiResponse.success(null));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponse response = orderService.cancelOrder(testOrder.getId(), userId);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(productClient).releaseInventory(any(UUID.class), anyInt(), any(UUID.class));
    }

    @Test
    @DisplayName("Should not cancel shipped order")
    void cancelOrder_ShippedOrder_ThrowsException() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findByIdWithItems(testOrder.getId())).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(testOrder.getId(), userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel");
    }

    @Test
    @DisplayName("Should update order status")
    void updateOrderStatus_Success() {
        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.updateOrderStatus(testOrder.getId(), OrderStatus.CONFIRMED);

        verify(orderRepository).save(argThat(order -> 
                order.getStatus() == OrderStatus.CONFIRMED && order.getPaidAt() != null));
    }
}
