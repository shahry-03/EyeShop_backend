package com.eyeshop.category.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryResponse(
    Long id,
    String name,
    String slug,
    String description,
    Long parentId,
    List<CategoryResponse> children, // Nested structure frontend ke liye
    LocalDateTime createdAt
) {}