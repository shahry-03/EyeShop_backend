package com.eyeshop.category.mapper;

import com.eyeshop.category.dto.response.CategoryResponse;
import com.eyeshop.category.entity.Category;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CategoryMapper {
    private CategoryMapper() {}

    public static CategoryResponse toResponse(Category category, boolean includeChildren) {
        if (category == null) return null;
        
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getDescription(),
            category.getParent() != null ? category.getParent().getId() : null,
            includeChildren ? toResponseList(category.getChildren(), true) : Collections.emptyList(),
            category.getCreatedAt()
        );
    }

    public static List<CategoryResponse> toResponseList(List<Category> categories, boolean includeChildren) {
        if (categories == null || categories.isEmpty()) return Collections.emptyList();
        return categories.stream()
            .map(c -> toResponse(c, includeChildren))
            .collect(Collectors.toList());
    }
}