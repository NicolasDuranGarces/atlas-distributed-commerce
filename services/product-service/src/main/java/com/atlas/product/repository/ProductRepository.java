package com.atlas.product.repository;

import com.atlas.product.entity.Product;
import com.atlas.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Product entity with search and inventory operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > p.reservedQuantity")
    Page<Product> findAvailableProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.featured = true AND p.status = 'ACTIVE'")
    List<Product> findFeaturedProducts();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    // Optimistic locking for inventory
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE Product p SET p.reservedQuantity = p.reservedQuantity + :quantity WHERE p.id = :productId AND p.stockQuantity - p.reservedQuantity >= :quantity")
    int reserveStock(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.reservedQuantity = p.reservedQuantity - :quantity WHERE p.id = :productId AND p.reservedQuantity >= :quantity")
    int releaseStock(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity, p.reservedQuantity = p.reservedQuantity - :quantity WHERE p.id = :productId")
    int confirmSale(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity - p.reservedQuantity <= p.lowStockThreshold AND p.status = 'ACTIVE'")
    List<Product> findLowStockProducts();
}
