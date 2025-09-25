package com.pitang.booster_c1m1.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pitang.booster_c1m1.domain.Product;
import com.pitang.booster_c1m1.dto.CreateProductDTO;
import com.pitang.booster_c1m1.dto.ProductDTO;
import com.pitang.booster_c1m1.mapper.ProductMapper;
import com.pitang.booster_c1m1.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.Counter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private static final ProductMapper MAPPER = ProductMapper.INSTANCE;

    private final ProductRepository productRepository;
    private final Counter productCreatedCounter;
    private final Counter productUpdatedCounter;
    private final Counter productDeletedCounter;
    private final Counter productNotFoundCounter;

    public Page<ProductDTO> getAllProducts(Pageable pageable, String name) {
        log.debug("Fetching products from database - name filter: {}", name);
        Page<Product> products;
        if (name != null) {
            products = productRepository.findByNameContainingIgnoreCase(name, pageable);
            log.debug("Found {} products matching name '{}'", products.getTotalElements(), name);
        } else {
            products = productRepository.findAll(pageable);
            log.debug("Found {} total products", products.getTotalElements());
        }
        return products.map(MAPPER::toDto);
    }

    public ProductDTO getProductById(Long id) {
        log.debug("Searching for product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    productNotFoundCounter.increment();
                    log.warn("Product not found with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                });
        log.debug("Product found: {}", product.getName());
        return MAPPER.toDto(product);
    }

    public ProductDTO createProduct(CreateProductDTO createProductDTO) {
        log.debug("Attempting to create product with name: {}", createProductDTO.getName());
        Product product = MAPPER.toProduct(createProductDTO);

        product.setCreatedAt(Instant.now().toString());
        Product savedProduct = productRepository.save(product);
        productCreatedCounter.increment();
        log.info("Product created successfully with id: {} and name: {}", savedProduct.getId(), savedProduct.getName());

        return MAPPER.toDto(savedProduct);
    }

    public ProductDTO updateProduct(Long id, CreateProductDTO createProductDTO) {
        log.debug("Attempting to update product with id: {}", id);
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> {
            productNotFoundCounter.increment();
            log.warn("Product not found with id: {}", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        });

        MAPPER.updateProductFromDTO(createProductDTO, existingProduct);
        existingProduct.setUpdatedAt(Instant.now().toString());
        Product updatedProduct = productRepository.save(existingProduct);
        productUpdatedCounter.increment();
        log.info("Product updated successfully with id: {} and name: {}", updatedProduct.getId(), updatedProduct.getName());

        return MAPPER.toDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        log.debug("Attempting to delete product with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid product id provided for deletion: {}", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product ID");
        }

        if (!productRepository.existsById(id)) {
            productNotFoundCounter.increment();
            log.warn("Attempt to delete non-existent product with id: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);
        productDeletedCounter.increment();
        log.info("Product with id {} deleted successfully", id);
    }
}