package com.eyeshop.product.dto.response;

import com.eyeshop.category.dto.response.CategoryResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
    Long id,
    String name,
    String description,
    double price,
    int stock,
    CategoryResponse category,
    List<ProductImageResponse> images,      
    LocalDateTime createdAt
) {}