package com.atlas.product.service;

import com.atlas.common.dto.PagedResponse;
import com.atlas.common.event.InventoryUpdatedEvent;
import com.atlas.common.exception.BusinessException;
import com.atlas.common.exception.InsufficientStockException;
import com.atlas.common.exception.ResourceNotFoundException;
import com.atlas.product.dto.*;
import com.atlas.product.entity.Category;
import com.atlas.product.entity.Product;
import com.atlas.product.entity.ProductStatus;
import com.atlas.product.repository.CategoryRepository;
import com.atlas.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for product and inventory operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String PRODUCT_EXCHANGE = "product.exchange";

    /**
     * Create a new product.
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("Product with SKU already exists: " + request.getSku());
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .costPrice(request.getCostPrice())
                .category(category)
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .additionalImages(request.getAdditionalImages())
                .brand(request.getBrand())
                .weight(request.getWeight())
                .weightUnit(request.getWeightUnit() != null ? request.getWeightUnit() : "kg")
                .tags(request.getTags())
                .status(ProductStatus.ACTIVE)
                .build();

        product = productRepository.save(product);
        log.info("Product created with ID: {}", product.getId());

        return mapToResponse(product);
    }

    /**
     * Get product by ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return mapToResponse(product);
    }

    /**
     * Search products with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(String query, Pageable pageable) {
        Page<Product> page = productRepository.searchProducts(query, pageable);
        return mapToPagedResponse(page);
    }

    /**
     * Get products by category.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'category-' + #categoryId + '-' + #pageable.pageNumber")
    public PagedResponse<ProductResponse> getProductsByCategory(UUID categoryId, Pageable pageable) {
        Page<Product> page = productRepository.findByCategoryId(categoryId, pageable);
        return mapToPagedResponse(page);
    }

    /**
     * Reserve inventory for an order.
     */
    @Transactional
    public void reserveInventory(UUID productId, int quantity, UUID orderId) {
        log.info("Reserving {} units of product {} for order {}", quantity, productId, orderId);

        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(product.getName(), quantity, product.getAvailableQuantity());
        }

        int updated = productRepository.reserveStock(productId, quantity);
        if (updated == 0) {
            throw new InsufficientStockException("Failed to reserve stock - concurrent modification");
        }

        // Publish event
        InventoryUpdatedEvent event = InventoryUpdatedEvent.reserved(
                productId, product.getSku(),
                product.getStockQuantity(), product.getStockQuantity() - quantity,
                orderId
        );
        rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, "inventory.reserved", event);
        log.info("Inventory reserved successfully");
    }

    /**
     * Release reserved inventory (order cancelled).
     */
    @Transactional
    public void releaseInventory(UUID productId, int quantity, UUID orderId) {
        log.info("Releasing {} units of product {} for order {}", quantity, productId, orderId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        int updated = productRepository.releaseStock(productId, quantity);
        if (updated == 0) {
            log.warn("Failed to release stock for product {}", productId);
        }

        // Publish event
        InventoryUpdatedEvent event = InventoryUpdatedEvent.released(
                productId, product.getSku(),
                product.getStockQuantity(), product.getStockQuantity() + quantity,
                orderId
        );
        rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, "inventory.released", event);
    }

    /**
     * Confirm sale (order completed).
     */
    @Transactional
    public void confirmSale(UUID productId, int quantity) {
        productRepository.confirmSale(productId, quantity);
    }

    /**
     * Get featured products.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'featured'")
    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findFeaturedProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .discountPercentage(product.getDiscountPercentage())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .stockQuantity(product.getStockQuantity())
                .availableQuantity(product.getAvailableQuantity())
                .inStock(product.isInStock())
                .lowStock(product.isLowStock())
                .status(product.getStatus())
                .imageUrl(product.getImageUrl())
                .additionalImages(product.getAdditionalImages())
                .brand(product.getBrand())
                .weight(product.getWeight())
                .weightUnit(product.getWeightUnit())
                .featured(product.getFeatured())
                .tags(product.getTags())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private PagedResponse<ProductResponse> mapToPagedResponse(Page<Product> page) {
        List<ProductResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
