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
    SellerSummary seller, // <-- YEH ADD HUA
    List<ProductImageResponse> images,
    Double averageRating,
    Integer totalReviews,
    LocalDateTime createdAt
) {
    // Nested record for Seller info
    public record SellerSummary(Long id, String shopName) {}
}