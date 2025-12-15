package com.atlas.order.client;

import com.atlas.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fallback for Product Client when service is unavailable.
 */
@Slf4j
@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ApiResponse<ProductInfo> getProduct(UUID productId) {
        log.warn("Product service unavailable, returning fallback for product: {}", productId);
        return ApiResponse.error("Product service temporarily unavailable", "SERVICE_UNAVAILABLE");
    }

    @Override
    public ApiResponse<Void> reserveInventory(UUID productId, int quantity, UUID orderId) {
        log.warn("Product service unavailable, cannot reserve inventory for product: {}", productId);
        return ApiResponse.error("Product service temporarily unavailable", "SERVICE_UNAVAILABLE");
    }

    @Override
    public ApiResponse<Void> releaseInventory(UUID productId, int quantity, UUID orderId) {
        log.warn("Product service unavailable, cannot release inventory for product: {}", productId);
        return ApiResponse.error("Product service temporarily unavailable", "SERVICE_UNAVAILABLE");
    }
}
