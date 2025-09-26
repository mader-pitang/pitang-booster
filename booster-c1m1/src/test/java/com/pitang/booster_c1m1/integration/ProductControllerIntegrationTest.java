package com.pitang.booster_c1m1.integration;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitang.booster_c1m1.config.BaseIntegrationTest;
import com.pitang.booster_c1m1.domain.Product;
import com.pitang.booster_c1m1.dto.CreateProductDTO;
import com.pitang.booster_c1m1.repository.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("ProductController Integration Tests")
public class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private CreateProductDTO createProductDTO;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct = Product.builder()
                .name("Notebook Dell")
                .description("High performance notebook")
                .price(new BigDecimal("2999.99"))
                .quantity(10)
                .category("Electronics")
                .build();

        createProductDTO = CreateProductDTO.builder()
                .name("New Product")
                .description("New product description")
                .price(new BigDecimal("199.99"))
                .quantity(5)
                .category("Category")
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_ShouldCreateProduct_WhenValidData() throws Exception {
        String jsonContent = objectMapper.writeValueAsString(createProductDTO);

        mockMvc.perform(post("/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.description").value("New product description"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.category").value("Category"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("New Product");
        assertThat(products.get(0).getPrice()).isEqualTo(new BigDecimal("199.99"));
    }

    @Test
    @DisplayName("Should return 400 when creating product with invalid data")
    void createProduct_ShouldReturn400_WhenInvalidData() throws Exception {
        CreateProductDTO invalidDTO = CreateProductDTO.builder()
                .name("") // Empty name
                .price(new BigDecimal("-10.00")) // Negative price
                .quantity(-5) // Negative quantity
                .build();

        String jsonContent = objectMapper.writeValueAsString(invalidDTO);

        mockMvc.perform(post("/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());

        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(0);
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void getAllProducts_ShouldReturnPagedProducts_WhenProductsExist() throws Exception {
        productRepository.save(testProduct);

        mockMvc.perform(get("/v1/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Notebook Dell"))
                .andExpect(jsonPath("$.content[0].price").value(2999.99))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should get empty list when no products exist")
    void getAllProducts_ShouldReturnEmptyList_WhenNoProductsExist() throws Exception {
        mockMvc.perform(get("/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should get product by ID")
    void getProductById_ShouldReturnProduct_WhenProductExists() throws Exception {
        Product savedProduct = productRepository.save(testProduct);

        mockMvc.perform(get("/v1/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Notebook Dell"))
                .andExpect(jsonPath("$.price").value(2999.99));
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void getProductById_ShouldReturn404_WhenProductNotFound() throws Exception {
        mockMvc.perform(get("/v1/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_ShouldUpdateProduct_WhenValidData() throws Exception {
        Product savedProduct = productRepository.save(testProduct);

        CreateProductDTO updateDTO = CreateProductDTO.builder()
                .name("Updated Notebook")
                .description("Updated description")
                .price(new BigDecimal("3299.99"))
                .quantity(15)
                .category("Updated Electronics")
                .build();

        String jsonContent = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(put("/v1/products/{id}", savedProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Notebook"))
                .andExpect(jsonPath("$.price").value(3299.99))
                .andExpect(jsonPath("$.quantity").value(15))
                .andExpect(jsonPath("$.updatedAt").exists());

        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElse(null);
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getName()).isEqualTo("Updated Notebook");
        assertThat(updatedProduct.getPrice()).isEqualTo(new BigDecimal("3299.99"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent product")
    void updateProduct_ShouldReturn404_WhenProductNotFound() throws Exception {
        String jsonContent = objectMapper.writeValueAsString(createProductDTO);

        mockMvc.perform(put("/v1/products/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_ShouldDeleteProduct_WhenProductExists() throws Exception {
        Product savedProduct = productRepository.save(testProduct);

        mockMvc.perform(delete("/v1/products/{id}", savedProduct.getId()))
                .andExpect(status().isNoContent());

        assertThat(productRepository.findById(savedProduct.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent product")
    void deleteProduct_ShouldReturn404_WhenProductNotFound() throws Exception {
        mockMvc.perform(delete("/v1/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should filter products by name")
    void getAllProducts_ShouldFilterByName_WhenNameParameterProvided() throws Exception {
        Product product1 = Product.builder()
                .name("Notebook Dell")
                .price(new BigDecimal("2999.99"))
                .quantity(5)
                .build();

        Product product2 = Product.builder()
                .name("Mouse Logitech")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        productRepository.save(product1);
        productRepository.save(product2);

        mockMvc.perform(get("/v1/products")
                .param("name", "Notebook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Notebook Dell"));
    }
}