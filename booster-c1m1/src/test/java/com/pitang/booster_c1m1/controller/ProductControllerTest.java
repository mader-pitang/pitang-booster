package com.pitang.booster_c1m1.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.pitang.booster_c1m1.domain.Product;
import com.pitang.booster_c1m1.dto.CreateProductDTO;
import com.pitang.booster_c1m1.dto.PaginatedResponseDTO;
import com.pitang.booster_c1m1.dto.ProductDTO;
import com.pitang.booster_c1m1.mapper.ProductMapper;
import com.pitang.booster_c1m1.service.ProductService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController")
public class ProductControllerTest {

  @Mock
  private ProductService productService;

  private ProductMapper productMapper = ProductMapper.INSTANCE;

  @InjectMocks
  private ProductController productController;

  private Product product;
  private Product anotherProduct;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    pageable = PageRequest.of(0, 10);
    product = Product.builder()
        .id(1L)
        .name("Notebook Dell")
        .description("High performance notebook")
        .price(new BigDecimal("2999.99"))
        .quantity(10)
        .category("Electronics")
        .createdAt(Instant.now().toString())
        .updatedAt(Instant.now().toString())
        .build();

    anotherProduct = Product.builder()
        .id(2L)
        .name("Mouse Logitech")
        .description("Wireless gaming mouse")
        .price(new BigDecimal("199.99"))
        .quantity(25)
        .category("Accessories")
        .createdAt(Instant.now().toString())
        .updatedAt(Instant.now().toString())
        .build();
  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return all products paged when successful")
  void getAllProducts_ReturnsPagedProducts_WhenSuccessful() {
    List<ProductDTO> productDTOs = Arrays.asList(productMapper.toDto(product), productMapper.toDto(anotherProduct));
    Page<ProductDTO> productPage = new PageImpl<>(productDTOs, pageable, productDTOs.size());

    when(productService.getAllProducts(pageable, null)).thenReturn(productPage);

    ResponseEntity<PaginatedResponseDTO<ProductDTO>> response = productController.getAllProducts(pageable, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(2);
    assertThat(response.getBody().getContent()).containsExactlyElementsOf(productDTOs);
    assertThat(response.getBody().getPage()).isEqualTo(0);
    assertThat(response.getBody().getSize()).isEqualTo(10);

    verify(productService).getAllProducts(pageable, null);
  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return empty list when no products are found")
  void getAllProducts_ReturnsEmptyList_WhenNoProductsFound() {
    Page<ProductDTO> emptyPage = Page.empty(pageable);
    when(productService.getAllProducts(pageable, null)).thenReturn(emptyPage);

    ResponseEntity<PaginatedResponseDTO<ProductDTO>> response = productController.getAllProducts(pageable, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).isEmpty();
    assertThat(response.getBody().getPage()).isEqualTo(0);
    assertThat(response.getBody().getSize()).isEqualTo(10);
    verify(productService).getAllProducts(pageable, null);
  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return filtered products when name parameter is provided")
  void getAllProducts_ReturnsFilteredProducts_WhenNameParameterProvided() {
    String nameFilter = "Notebook";
    List<ProductDTO> productDTOs = Arrays.asList(productMapper.toDto(product));
    Page<ProductDTO> productPage = new PageImpl<>(productDTOs, pageable, productDTOs.size());
    when(productService.getAllProducts(pageable, nameFilter)).thenReturn(productPage);

    ResponseEntity<PaginatedResponseDTO<ProductDTO>> response = productController.getAllProducts(pageable, nameFilter);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(1);
    assertThat(response.getBody().getContent()).containsExactlyElementsOf(productDTOs);
    assertThat(response.getBody().getPage()).isEqualTo(0);
    assertThat(response.getBody().getSize()).isEqualTo(10);
    verify(productService).getAllProducts(pageable, nameFilter);
  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return empty list when no products match the name filter")
  void getAllProducts_ReturnsEmptyList_WhenNoProductsMatchNameFilter() {
    String nameFilter = "NonExistentProduct";
    Page<ProductDTO> emptyPage = Page.empty(pageable);
    when(productService.getAllProducts(pageable, nameFilter)).thenReturn(emptyPage);

    ResponseEntity<PaginatedResponseDTO<ProductDTO>> response = productController.getAllProducts(pageable, nameFilter);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).isEmpty();
    assertThat(response.getBody().getPage()).isEqualTo(0);
    assertThat(response.getBody().getSize()).isEqualTo(10);
    verify(productService).getAllProducts(pageable, nameFilter);
  }

  @Test
  @DisplayName("Should return product by ID when successful")
  void getProductById_ReturnsProduct_WhenSuccessful() {
    Long productId = 1L;
    ProductDTO productDTO = productMapper.toDto(product);
    when(productService.getProductById(productId)).thenReturn(productDTO);

    ResponseEntity<ProductDTO> response = productController.getProductById(productId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isEqualTo(productDTO);
    verify(productService).getProductById(productId);
  }

  @Test
  @DisplayName("Should return 404 when product not found by ID")
  void getProductById_Returns404_WhenProductNotFound() {
    Long productId = 999L;
    when(productService.getProductById(productId))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

    assertThatThrownBy(() -> productController.getProductById(productId))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
        .hasMessageContaining("Product not found");
    verify(productService).getProductById(productId);
  }

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should create product when successful")
  void createProduct_CreatesProduct_WhenSuccessful() {
    CreateProductDTO createProductDTO = CreateProductDTO.builder()
        .name("New Product")
        .description("New product description")
        .price(new BigDecimal("99.99"))
        .quantity(5)
        .category("Category")
        .build();

    ProductDTO createdProductDTO = new ProductDTO();
    createdProductDTO.setId(1L);
    createdProductDTO.setName("New Product");
    createdProductDTO.setDescription("New product description");
    createdProductDTO.setPrice(new BigDecimal("99.99"));
    createdProductDTO.setQuantity(5);
    createdProductDTO.setCategory("Category");
    createdProductDTO.setCreatedAt(Instant.now().toString());
    createdProductDTO.setUpdatedAt(Instant.now().toString());

    when(productService.createProduct(createProductDTO)).thenReturn(createdProductDTO);

    ResponseEntity<ProductDTO> response = productController.createProduct(createProductDTO);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getName()).isEqualTo("New Product");
    assertThat(response.getBody().getPrice()).isEqualTo(new BigDecimal("99.99"));
    assertThat(response.getBody().getId()).isNotNull();
    verify(productService).createProduct(createProductDTO);
  }

  @Test
  @DisplayName("Should update product when successful")
  void updateProduct_UpdatesProduct_WhenSuccessful() {
    Long productId = 1L;
    CreateProductDTO updateDTO = CreateProductDTO.builder()
        .name("Updated Product")
        .description("Updated description")
        .price(new BigDecimal("199.99"))
        .quantity(15)
        .category("Updated Category")
        .build();

    ProductDTO updatedProductDTO = new ProductDTO();
    updatedProductDTO.setId(productId);
    updatedProductDTO.setName("Updated Product");
    updatedProductDTO.setDescription("Updated description");
    updatedProductDTO.setPrice(new BigDecimal("199.99"));
    updatedProductDTO.setQuantity(15);
    updatedProductDTO.setCategory("Updated Category");
    updatedProductDTO.setCreatedAt(product.getCreatedAt());
    updatedProductDTO.setUpdatedAt(Instant.now().toString());

    when(productService.updateProduct(productId, updateDTO)).thenReturn(updatedProductDTO);

    ResponseEntity<ProductDTO> response = productController.updateProduct(productId, updateDTO);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isEqualTo(updatedProductDTO);
    verify(productService).updateProduct(productId, updateDTO);
  }

  @Test
  @DisplayName("Should return 404 when updating non-existent product")
  void updateProduct_Returns404_WhenProductNotFound() {
    Long productId = 999L;
    CreateProductDTO updateDTO = CreateProductDTO.builder()
        .name("Non Existent")
        .price(new BigDecimal("99.99"))
        .build();

    when(productService.updateProduct(productId, updateDTO))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

    assertThatThrownBy(() -> productController.updateProduct(productId, updateDTO))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
        .hasMessageContaining("Product not found");

    verify(productService).updateProduct(productId, updateDTO);
  }

  @Test
  @DisplayName("Should delete product when successful")
  void deleteProduct_DeletesProduct_WhenSuccessful() {
    Long productId = 1L;

    ResponseEntity<Void> response = productController.deleteProduct(productId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
    verify(productService).deleteProduct(productId);
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent product")
  void deleteProduct_Returns404_WhenProductNotFound() {
    Long productId = 999L;
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")).when(productService).deleteProduct(productId);

    assertThatThrownBy(() -> productController.deleteProduct(productId))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
        .hasMessageContaining("Product not found");

    verify(productService).deleteProduct(productId);
  }

  @Test
  @DisplayName("Should return 400 when deleting with invalid id")
  void deleteProduct_Returns400_WhenInvalidId() {
    Long invalidId = 0L;
    doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product ID")).when(productService)
        .deleteProduct(invalidId);

    assertThatThrownBy(() -> productController.deleteProduct(invalidId))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
        .hasMessageContaining("Invalid product ID");

    verify(productService).deleteProduct(invalidId);
  }

  @Test
  @DisplayName("Should fail validation when create DTO is invalid")
  void createProduct_Returns400_WhenInvalidInput() {
    CreateProductDTO invalidDTO = CreateProductDTO.builder()
        .name("")
        .price(new BigDecimal("-10.00"))
        .quantity(-5)
        .build();

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    Set<ConstraintViolation<CreateProductDTO>> violations = validator.validate(invalidDTO);

    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .anyMatch(v -> v.getMessage().contains("Name is required") || v.getPropertyPath().toString().equals("name"));
    assertThat(violations).anyMatch(
        v -> v.getMessage().contains("Price must be greater than 0") || v.getPropertyPath().toString().equals("price"));
    assertThat(violations).anyMatch(v -> v.getMessage().contains("Quantity cannot be negative")
        || v.getPropertyPath().toString().equals("quantity"));

    verify(productService, org.mockito.Mockito.never()).createProduct(org.mockito.Mockito.any());
  }
}