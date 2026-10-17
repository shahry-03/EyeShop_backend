package com.eyeshop.product.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
    Long id,
    String name,
    String description,
    double price,
    int stock,
    String category,
    List<ProductImageResponse> images,      // ← ADD
    LocalDateTime createdAt
) {}