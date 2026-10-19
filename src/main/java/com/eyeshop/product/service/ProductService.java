package com.eyeshop.product.service;

import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.product.dto.request.ProductAddRequest;
import com.eyeshop.product.dto.request.ProductUpdateRequest;
import com.eyeshop.product.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    ProductResponse addProduct(UUID userId, boolean isAdmin, ProductAddRequest request);

    ProductResponse updateProduct(Long id, UUID userId, boolean isAdmin, ProductUpdateRequest request);

    void deleteProduct(Long id, UUID userId, boolean isAdmin);

    ProductResponse getProductById(Long id);

    PageResponse<ProductResponse> searchProducts(String keyword, Long categoryId, Double minPrice, Double maxPrice, Double minRating, Boolean inStock, Pageable pageable);

    PageResponse<ProductResponse> getProductsByCategoryId(Long categoryId, Pageable pageable);

    // New Method Seller can see its own products
    PageResponse<ProductResponse> getSellerProducts(UUID userId, Pageable pageable);
}