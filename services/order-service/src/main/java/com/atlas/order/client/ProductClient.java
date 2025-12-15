package com.atlas.order.client;

import com.atlas.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Feign client for Product Service.
 */
@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/products/{productId}")
    ApiResponse<ProductInfo> getProduct(@PathVariable("productId") UUID productId);

    @PostMapping("/api/inventory/{productId}/reserve")
    ApiResponse<Void> reserveInventory(
            @PathVariable("productId") UUID productId,
            @RequestParam("quantity") int quantity,
            @RequestParam("orderId") UUID orderId);

    @PostMapping("/api/inventory/{productId}/release")
    ApiResponse<Void> releaseInventory(
            @PathVariable("productId") UUID productId,
            @RequestParam("quantity") int quantity,
            @RequestParam("orderId") UUID orderId);

    record ProductInfo(
            UUID id,
            String sku,
            String name,
            BigDecimal price,
            String imageUrl,
            Integer availableQuantity,
            Boolean inStock
    ) {}
}
