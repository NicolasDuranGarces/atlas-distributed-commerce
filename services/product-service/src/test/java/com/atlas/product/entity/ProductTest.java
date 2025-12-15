package com.atlas.product.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("Should create product with builder")
    void createProduct_WithBuilder() {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .sku("TEST-001")
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .reservedQuantity(10)
                .status(ProductStatus.ACTIVE)
                .build();

        assertThat(product.getSku()).isEqualTo("TEST-001");
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should calculate available quantity correctly")
    void getAvailableQuantity() {
        Product product = Product.builder()
                .stockQuantity(100)
                .reservedQuantity(20)
                .build();

        assertThat(product.getAvailableQuantity()).isEqualTo(80);
    }

    @Test
    @DisplayName("Should check if in stock correctly")
    void isInStock() {
        Product inStockProduct = Product.builder()
                .stockQuantity(100)
                .reservedQuantity(50)
                .lowStockThreshold(10)
                .status(ProductStatus.ACTIVE)
                .build();

        Product outOfStockProduct = Product.builder()
                .stockQuantity(100)
                .reservedQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();

        Product inactiveProduct = Product.builder()
                .stockQuantity(100)
                .reservedQuantity(0)
                .status(ProductStatus.INACTIVE)
                .build();

        assertThat(inStockProduct.isInStock()).isTrue();
        assertThat(outOfStockProduct.isInStock()).isFalse();
        assertThat(inactiveProduct.isInStock()).isFalse();
    }

    @Test
    @DisplayName("Should check if low stock correctly")
    void isLowStock() {
        Product lowStockProduct = Product.builder()
                .stockQuantity(15)
                .reservedQuantity(10)
                .lowStockThreshold(10)
                .build();

        Product normalStockProduct = Product.builder()
                .stockQuantity(100)
                .reservedQuantity(10)
                .lowStockThreshold(10)
                .build();

        assertThat(lowStockProduct.isLowStock()).isTrue();
        assertThat(normalStockProduct.isLowStock()).isFalse();
    }

    @Test
    @DisplayName("Default values should be set correctly")
    void defaultValues() {
        Product product = Product.builder()
                .name("Test")
                .sku("SKU-001")
                .build();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.getStockQuantity()).isZero();
        assertThat(product.getReservedQuantity()).isZero();
        assertThat(product.getLowStockThreshold()).isEqualTo(10);
        assertThat(product.isFeatured()).isFalse();
    }

    @Test
    @DisplayName("Should calculate discount percentage")
    void getDiscountPercentage() {
        Product productWithDiscount = Product.builder()
                .price(new BigDecimal("80.00"))
                .compareAtPrice(new BigDecimal("100.00"))
                .build();

        Product productWithoutDiscount = Product.builder()
                .price(new BigDecimal("80.00"))
                .build();

        assertThat(productWithDiscount.getDiscountPercentage()).isEqualTo(20);
        assertThat(productWithoutDiscount.getDiscountPercentage()).isEqualTo(0);
    }
}
