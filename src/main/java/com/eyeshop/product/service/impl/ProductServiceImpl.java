package com.eyeshop.product.service.impl;

import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.category.entity.Category;
import com.eyeshop.category.repository.CategoryRepository;
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
import com.eyeshop.product.specification.ProductSpecification;
import com.eyeshop.seller.entity.Seller;
import com.eyeshop.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;

    // ═══════════════════════════════════════════════════════════
    //  CREATE (Admin ya Approved Seller)
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public ProductResponse addProduct(UUID userId, boolean isAdmin, ProductAddRequest request) {
        if (productRepository.existsByName(request.name())) {
            throw new DuplicateProductException("Product with this name already exists: " + request.name());
        }

        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        Seller seller = null;
        // Agar Admin nahi hai, toh Seller check karo
        if (!isAdmin) {
            seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new AccessDeniedException("You must be an approved seller to add products."));
            
            if (!seller.isApproved()) {
                throw new AccessDeniedException("Your seller account is pending approval.");
            }
        }

        Product product = Product.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .stock(request.stock())
            .category(category)
            .seller(seller) // Agar seller hai toh link hoga, warna null (Admin product)
            .build();

        Product saved = productRepository.saveAndFlush(product);
        log.info("Product created: id={}, name={}, sellerId={}", saved.getId(), saved.getName(), seller != null ? seller.getId() : "ADMIN");

        return ProductMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  UPDATE (Sirf Owner ya Admin)
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UUID userId, boolean isAdmin, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

        // Security Check: Agar Admin nahi hai, toh check karo ki product usi ka hai ya nahi
        if (!isAdmin) {
            if (product.getSeller() == null || !product.getSeller().getUser().getId().equals(userId)) {
                throw new AccessDeniedException("You can only update your own products.");
            }
        }

        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);

        Product updated = productRepository.saveAndFlush(product);
        log.info("Product updated: id={}, by userId={}", id, userId);

        return ProductMapper.toResponse(updated);
    }

    // ═══════════════════════════════════════════════════════════
    //  DELETE (Sirf Owner ya Admin)
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public void deleteProduct(Long id, UUID userId, boolean isAdmin) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

        if (!isAdmin) {
            if (product.getSeller() == null || !product.getSeller().getUser().getId().equals(userId)) {
                throw new AccessDeniedException("You can only delete your own products.");
            }
        }

        productRepository.delete(product);
        log.info("Product deleted: id={}, by userId={}", id, userId);
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

    // ═══════════════════════════════════════════════════════════
    //  SEARCH & FILTER
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProducts(
            String keyword, Long categoryId, Double minPrice, Double maxPrice, Double minRating, Boolean inStock, Pageable pageable) {

        Specification<Product> spec = ProductSpecification.withFilters(
            keyword, categoryId, minPrice, maxPrice, minRating, inStock);
        Page<Product> page = productRepository.findAll(spec, pageable);

        return PageResponse.from(page, ProductMapper::toResponse);
    }

    // ═══════════════════════════════════════════════════════════
    //  GET BY CATEGORY
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        Page<Product> page = productRepository.findByCategoryId(categoryId, pageable);
        return PageResponse.from(page, ProductMapper::toResponse);
    }

    // ═══════════════════════════════════════════════════════════
    //  GET SELLER PRODUCTS (For Seller Dashboard)
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getSellerProducts(UUID userId, Pageable pageable) {
        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new AccessDeniedException("You are not a registered seller."));

        Page<Product> page = productRepository.findBySellerId(seller.getId(), pageable);
        return PageResponse.from(page, ProductMapper::toResponse);
    }
}