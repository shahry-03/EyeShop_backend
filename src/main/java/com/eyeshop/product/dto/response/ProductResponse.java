package com.eyeshop.product.dto.response;

import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    String name,
    String description,
    double price,
    int stock,
    String category,
    LocalDateTime createdAt
) {}