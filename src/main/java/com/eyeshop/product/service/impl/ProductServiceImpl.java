package com.eyeshop.product.service.impl;

import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.product.dto.request.ProductAddRequest;
import com.eyeshop.product.dto.request.ProductUpdateRequest;
import com.eyeshop.product.dto.response.ProductResponse;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.exception.DuplicateProductException;
import com.eyeshop.product.exception.ProductNotFoundException;
import com.eyeshop.product.mapper.ProductMapper;
import com.eyeshop.product.repository.ProductRepository;
import com.eyeshop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eyeshop.category.entity.Category; 
import com.eyeshop.category.repository.CategoryRepository; 
import com.eyeshop.auth.exception.ResourceNotFoundException; 

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ═══════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public ProductResponse addProduct(ProductAddRequest request) {
        if (productRepository.existsByName(request.name())) {
            throw new DuplicateProductException(
                "Product with this name already exists: " + request.name());
        }

        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found: " + request.categoryId()
            ));

        Product product = Product.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .stock(request.stock())
            .category(category)
            .build();

        Product saved = productRepository.saveAndFlush(product);
        log.info("Product created: id={}, name={}", saved.getId(), saved.getName());

        return ProductMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  UPDATE
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found: " + request.categoryId()
            ));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);

        Product updated = productRepository.saveAndFlush(product);
        log.info("Product updated: id={}", id);

        return ProductMapper.toResponse(updated);
    }

    // ═══════════════════════════════════════════════════════════
    //  DELETE
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted: id={}", id);
    }

    // ═══════════════════════════════════════════════════════════
    //  READ
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        return ProductMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return PageResponse.from(page, ProductMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        Page<Product> page = productRepository.findByCategoryId(categoryId, pageable);
        return PageResponse.from(page, ProductMapper::toResponse);
    }
}