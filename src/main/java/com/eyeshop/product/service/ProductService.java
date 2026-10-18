package com.eyeshop.product.service;

import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.product.dto.request.ProductAddRequest;
import com.eyeshop.product.dto.request.ProductUpdateRequest;
import com.eyeshop.product.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse addProduct(ProductAddRequest request);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    void deleteProduct(Long id);

    ProductResponse getProductById(Long id);

    PageResponse<ProductResponse> searchProducts(
            String keyword, 
            Long categoryId, 
            Double minPrice, 
            Double maxPrice, 
            Double minRating, 
            Boolean inStock, 
            Pageable pageable);

    
    PageResponse<ProductResponse> getProductsByCategoryId(Long categoryId, Pageable pageable);
}