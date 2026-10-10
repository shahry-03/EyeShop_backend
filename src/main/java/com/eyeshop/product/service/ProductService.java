package com.eyeshop.product.service;

import com.eyeshop.product.dto.request.ProductAddRequest;
import com.eyeshop.product.dto.request.ProductUpdateRequest;
import com.eyeshop.product.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse addProduct(ProductAddRequest request);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    void deleteProduct(Long id);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductsByCategory(String category);
}