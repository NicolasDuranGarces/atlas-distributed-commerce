package com.atlas.product.service;

import com.atlas.common.dto.PagedResponse;
import com.atlas.common.exception.BusinessException;
import com.atlas.common.exception.InsufficientStockException;
import com.atlas.common.exception.ResourceNotFoundException;
import com.atlas.product.dto.CreateProductRequest;
import com.atlas.product.dto.ProductResponse;
import com.atlas.product.entity.Category;
import com.atlas.product.entity.Product;
import com.atlas.product.entity.ProductStatus;
import com.atlas.product.repository.CategoryRepository;
import com.atlas.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;
    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Electronics")
                .slug("electronics")
                .active(true)
                .build();

        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .sku("PROD-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .reservedQuantity(0)
                .status(ProductStatus.ACTIVE)
                .category(testCategory)
                .build();

        createRequest = new CreateProductRequest();
        createRequest.setSku("NEW-001");
        createRequest.setName("New Product");
        createRequest.setDescription("New Description");
        createRequest.setPrice(new BigDecimal("149.99"));
        createRequest.setStockQuantity(50);
        createRequest.setCategoryId(testCategory.getId());
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_Success() {
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProductResponse response = productService.createProduct(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("New Product");
        assertThat(response.getSku()).isEqualTo("NEW-001");

        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when SKU already exists")
    void createProduct_DuplicateSku_ThrowsException() {
        when(productRepository.existsBySku(anyString())).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SKU already exists");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should get product by ID")
    void getProduct_Success() {
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProduct(testProduct.getId());

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void getProduct_NotFound_ThrowsException() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should search products")
    void searchProducts_Success() {
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(productRepository.searchProducts(anyString(), any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> response = productService.searchProducts("test", 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Should get featured products")
    void getFeaturedProducts_Success() {
        when(productRepository.findFeaturedProducts(any(Pageable.class)))
                .thenReturn(List.of(testProduct));

        List<ProductResponse> response = productService.getFeaturedProducts(10);

        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("Should reserve inventory successfully")
    void reserveInventory_Success() {
        UUID orderId = UUID.randomUUID();
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(productRepository.reserveStock(testProduct.getId(), 10)).thenReturn(1);

        productService.reserveInventory(testProduct.getId(), 10, orderId);

        verify(productRepository).reserveStock(testProduct.getId(), 10);
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void reserveInventory_InsufficientStock_ThrowsException() {
        UUID orderId = UUID.randomUUID();
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(productRepository.reserveStock(testProduct.getId(), 10)).thenReturn(0);

        assertThatThrownBy(() -> productService.reserveInventory(testProduct.getId(), 10, orderId))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Should release inventory successfully")
    void releaseInventory_Success() {
        UUID orderId = UUID.randomUUID();
        testProduct.setReservedQuantity(10);
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(productRepository.releaseStock(testProduct.getId(), 5)).thenReturn(1);

        productService.releaseInventory(testProduct.getId(), 5, orderId);

        verify(productRepository).releaseStock(testProduct.getId(), 5);
    }

    @Test
    @DisplayName("Should confirm inventory successfully")
    void confirmInventory_Success() {
        UUID orderId = UUID.randomUUID();
        testProduct.setReservedQuantity(10);
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(productRepository.confirmStock(testProduct.getId(), 5)).thenReturn(1);

        productService.confirmInventory(testProduct.getId(), 5, orderId);

        verify(productRepository).confirmStock(testProduct.getId(), 5);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should get products by category")
    void getProductsByCategory_Success() {
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(productRepository.findByCategoryId(any(UUID.class), any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> response = productService.getProductsByCategory(testCategory.getId(), 0, 10);

        assertThat(response.getContent()).hasSize(1);
    }
}
