package com.pitang.booster_c1m1.integration;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.pitang.booster_c1m1.domain.Product;
import com.pitang.booster_c1m1.repository.ProductRepository;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository Integration Tests")
public class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;
    private Product anotherProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct = Product.builder()
                .name("Smartphone Samsung")
                .description("Smartphone Android com 128GB")
                .price(new BigDecimal("899.99"))
                .quantity(50)
                .category("Electronics")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        anotherProduct = Product.builder()
                .name("Notebook Dell")
                .description("Notebook com 16GB RAM e SSD 512GB")
                .price(new BigDecimal("2500.00"))
                .quantity(20)
                .category("Electronics")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    @Test
    @DisplayName("Should save product when valid product is provided")
    void save_SavesProduct_WhenValidProductProvided() {
        Product savedProduct = productRepository.save(testProduct);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Smartphone Samsung");
        assertThat(savedProduct.getPrice()).isEqualTo(new BigDecimal("899.99"));
        assertThat(savedProduct.getQuantity()).isEqualTo(50);
        assertThat(savedProduct.getCategory()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should find product by ID when product exists")
    void findById_ReturnsProduct_WhenProductExists() {
        Product savedProduct = productRepository.save(testProduct);

        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Smartphone Samsung");
        assertThat(foundProduct.get().getPrice()).isEqualTo(new BigDecimal("899.99"));
    }

    @Test
    @DisplayName("Should return empty when product not found by ID")
    void findById_ReturnsEmpty_WhenProductNotFound() {
        Optional<Product> foundProduct = productRepository.findById(999L);

        assertThat(foundProduct).isEmpty();
    }

    @Test
    @DisplayName("Should find all products with pagination")
    void findAll_ReturnsPagedProducts_WhenProductsExist() {
        productRepository.saveAll(List.of(testProduct, anotherProduct));
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productsPage = productRepository.findAll(pageable);

        assertThat(productsPage.getContent()).hasSize(2);
        assertThat(productsPage.getTotalElements()).isEqualTo(2);
        assertThat(productsPage.getTotalPages()).isEqualTo(1);
        assertThat(productsPage.getNumber()).isEqualTo(0);
        assertThat(productsPage.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return empty page when no products exist")
    void findAll_ReturnsEmptyPage_WhenNoProductsExist() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productsPage = productRepository.findAll(pageable);

        assertThat(productsPage.getContent()).isEmpty();
        assertThat(productsPage.getTotalElements()).isEqualTo(0);
        assertThat(productsPage.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should find products by name containing ignore case when products match")
    void findByNameContainingIgnoreCase_ReturnsMatchingProducts_WhenProductsMatch() {
        Product productWithSimilarName = Product.builder()
                .name("Smartphone iPhone")
                .description("iPhone com 256GB")
                .price(new BigDecimal("3500.00"))
                .quantity(15)
                .category("Electronics")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();

        productRepository.saveAll(List.of(testProduct, anotherProduct, productWithSimilarName));
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productsPage = productRepository.findByNameContainingIgnoreCase("smartphone", pageable);

        assertThat(productsPage.getContent()).hasSize(2);
        assertThat(productsPage.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Smartphone Samsung", "Smartphone iPhone");
    }

    @Test
    @DisplayName("Should delete product when product exists")
    void delete_DeletesProduct_WhenProductExists() {
        Product savedProduct = productRepository.save(testProduct);

        productRepository.delete(savedProduct);

        Optional<Product> deletedProduct = productRepository.findById(savedProduct.getId());
        assertThat(deletedProduct).isEmpty();
    }

    @Test
    @DisplayName("Should update product when product exists")
    void save_UpdatesProduct_WhenProductExists() {
        Product savedProduct = productRepository.save(testProduct);

        savedProduct.setName("Smartphone Samsung Atualizado");
        savedProduct.setPrice(new BigDecimal("799.99"));
        savedProduct.setQuantity(60);
        savedProduct.setUpdatedAt("2024-01-02T10:00:00Z");

        Product updatedProduct = productRepository.save(savedProduct);

        assertThat(updatedProduct.getId()).isEqualTo(savedProduct.getId());
        assertThat(updatedProduct.getName()).isEqualTo("Smartphone Samsung Atualizado");
        assertThat(updatedProduct.getPrice()).isEqualTo(new BigDecimal("799.99"));
        assertThat(updatedProduct.getQuantity()).isEqualTo(60);
        assertThat(updatedProduct.getUpdatedAt()).isEqualTo("2024-01-02T10:00:00Z");
    }

    @Test
    @DisplayName("Should count products correctly")
    void count_ReturnsCorrectCount_WhenProductsExist() {
        productRepository.saveAll(List.of(testProduct, anotherProduct));

        long count = productRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return zero count when no products exist")
    void count_ReturnsZero_WhenNoProductsExist() {
        long count = productRepository.count();

        assertThat(count).isEqualTo(0);
    }
}