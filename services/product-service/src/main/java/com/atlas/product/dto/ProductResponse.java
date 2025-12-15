package com.atlas.product.dto;

import com.atlas.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for product information responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal discountPercentage;
    private UUID categoryId;
    private String categoryName;
    private Integer stockQuantity;
    private Integer availableQuantity;
    private Boolean inStock;
    private Boolean lowStock;
    private ProductStatus status;
    private String imageUrl;
    private Set<String> additionalImages;
    private String brand;
    private Double weight;
    private String weightUnit;
    private Boolean featured;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
