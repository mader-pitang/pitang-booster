package com.pitang.booster_c1m1.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product Domain Tests")
public class ProductTest {

    @Test
    @DisplayName("Should create product with builder")
    void builder_CreatesProduct_WhenAllFieldsProvided() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .category("Test Category")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getDescription()).isEqualTo("Test Description");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(product.getQuantity()).isEqualTo(10);
        assertThat(product.getCategory()).isEqualTo("Test Category");
        assertThat(product.getCreatedAt()).isEqualTo("2024-01-01T10:00:00Z");
        assertThat(product.getUpdatedAt()).isEqualTo("2024-01-01T10:00:00Z");
    }

    @Test
    @DisplayName("Should create product with default quantity")
    void builder_CreatesProduct_WithDefaultQuantity() {
        Product product = Product.builder()
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .build();

        assertThat(product.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should create product with no-args constructor")
    void noArgsConstructor_CreatesProduct() {
        Product product = new Product();

        assertThat(product).isNotNull();
        assertThat(product.getId()).isNull();
        assertThat(product.getName()).isNull();
        assertThat(product.getDescription()).isNull();
        assertThat(product.getPrice()).isNull();
        assertThat(product.getQuantity()).isEqualTo(0);
        assertThat(product.getCategory()).isNull();
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should create product with all-args constructor")
    void allArgsConstructor_CreatesProduct() {
        Product product = new Product(1L, "Test Product", "Test Description",
                new BigDecimal("99.99"), 10, "Test Category",
                "2024-01-01T10:00:00Z", "2024-01-01T10:00:00Z");

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getDescription()).isEqualTo("Test Description");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(product.getQuantity()).isEqualTo(10);
        assertThat(product.getCategory()).isEqualTo("Test Category");
        assertThat(product.getCreatedAt()).isEqualTo("2024-01-01T10:00:00Z");
        assertThat(product.getUpdatedAt()).isEqualTo("2024-01-01T10:00:00Z");
    }

    @Test
    @DisplayName("Should be equal when products have same ID")
    void equals_ReturnsTrue_WhenProductsHaveSameId() {
        Product product1 = Product.builder().id(1L).name("Product 1").build();
        Product product2 = Product.builder().id(1L).name("Product 2").build();

        assertThat(product1).isEqualTo(product2);
        assertThat(product1.hashCode()).isEqualTo(product2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when products have different IDs")
    void equals_ReturnsFalse_WhenProductsHaveDifferentIds() {
        Product product1 = Product.builder().id(1L).name("Product 1").build();
        Product product2 = Product.builder().id(2L).name("Product 1").build();

        assertThat(product1).isNotEqualTo(product2);
        assertThat(product1.hashCode()).isNotEqualTo(product2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when compared with null")
    void equals_ReturnsFalse_WhenComparedWithNull() {
        Product product = Product.builder().id(1L).build();

        assertThat(product).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should be equal when compared with itself")
    void equals_ReturnsTrue_WhenComparedWithItself() {
        Product product = Product.builder().id(1L).build();

        assertThat(product).isEqualTo(product);
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void settersAndGetters_WorkCorrectly() {
        Product product = new Product();

        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setQuantity(10);
        product.setCategory("Test Category");
        product.setCreatedAt("2024-01-01T10:00:00Z");
        product.setUpdatedAt("2024-01-01T10:00:00Z");

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getDescription()).isEqualTo("Test Description");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(product.getQuantity()).isEqualTo(10);
        assertThat(product.getCategory()).isEqualTo("Test Category");
        assertThat(product.getCreatedAt()).isEqualTo("2024-01-01T10:00:00Z");
        assertThat(product.getUpdatedAt()).isEqualTo("2024-01-01T10:00:00Z");
    }
}