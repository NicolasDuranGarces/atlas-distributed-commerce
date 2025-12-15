package com.atlas.product.repository;

import com.atlas.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Category entity.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true ORDER BY c.displayOrder")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.active = true ORDER BY c.displayOrder")
    List<Category> findByParentId(UUID parentId);

    List<Category> findByActiveTrue();
}
