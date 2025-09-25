package com.pitang.booster_c1m1.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pitang.booster_c1m1.dto.CreateProductDTO;
import com.pitang.booster_c1m1.dto.PaginatedResponseDTO;
import com.pitang.booster_c1m1.dto.ProductDTO;
import com.pitang.booster_c1m1.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/products")
@Validated
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public ResponseEntity<PaginatedResponseDTO<ProductDTO>> getAllProducts(
      @PageableDefault(size = 10, page = 0) Pageable pageable, @RequestParam(required = false) String name) {
    log.info("getAllProducts - page: {}, size: {}, name: {}",
        pageable.getPageNumber(), pageable.getPageSize(), name);
    Page<ProductDTO> products = productService.getAllProducts(pageable, name);
    log.debug("Found {} products", products.getTotalElements());
    return ResponseEntity.ok(PaginatedResponseDTO.from(products));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
    log.info("getProductById - id: {}", id);
    ProductDTO product = productService.getProductById(id);
    return ResponseEntity.ok(product);
  }

  @PostMapping
  public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductDTO createProductDTO) {
    log.info("createProduct - name: {}", createProductDTO.getName());
    ProductDTO product = productService.createProduct(createProductDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(product);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody CreateProductDTO createProductDTO) {
    log.info("updateProduct - id: {}, name: {}", id, createProductDTO.getName());
    ProductDTO product = productService.updateProduct(id, createProductDTO);
    return ResponseEntity.ok(product);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    log.info("deleteProduct - id: {}", id);
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }
}