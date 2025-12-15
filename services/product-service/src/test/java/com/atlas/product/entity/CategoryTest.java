package com.atlas.product.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CategoryTest {

    @Test
    @DisplayName("Should create category with builder")
    void createCategory_WithBuilder() {
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name("Electronics")
                .slug("electronics")
                .description("Electronic products")
                .active(true)
                .build();

        assertThat(category.getName()).isEqualTo("Electronics");
        assertThat(category.getSlug()).isEqualTo("electronics");
        assertThat(category.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle parent-child relationship")
    void parentChildRelationship() {
        Category parent = Category.builder()
                .id(UUID.randomUUID())
                .name("Electronics")
                .slug("electronics")
                .build();

        Category child = Category.builder()
                .id(UUID.randomUUID())
                .name("Phones")
                .slug("phones")
                .parent(parent)
                .build();

        assertThat(child.getParent()).isEqualTo(parent);
        assertThat(child.isRootCategory()).isFalse();
    }

    @Test
    @DisplayName("Should identify root category")
    void isRootCategory() {
        Category rootCategory = Category.builder()
                .name("Root")
                .slug("root")
                .build();

        assertThat(rootCategory.isRootCategory()).isTrue();
    }

    @Test
    @DisplayName("Default values should be set correctly")
    void defaultValues() {
        Category category = Category.builder()
                .name("Test")
                .slug("test")
                .build();

        assertThat(category.isActive()).isTrue();
        assertThat(category.getSortOrder()).isZero();
    }

    @Test
    @DisplayName("Should get full path")
    void getFullPath() {
        Category parent = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .build();

        Category child = Category.builder()
                .name("Phones")
                .slug("phones")
                .parent(parent)
                .build();

        assertThat(child.getFullPath()).isEqualTo("Electronics > Phones");
        assertThat(parent.getFullPath()).isEqualTo("Electronics");
    }
}
