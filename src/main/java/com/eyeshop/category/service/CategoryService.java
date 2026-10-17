package com.eyeshop.category.service;

import com.eyeshop.category.dto.request.CategoryRequest;
import com.eyeshop.category.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getCategoryTree(); // Nested (Root > Children)
    List<CategoryResponse> getAllFlat();      // Flat list
    CategoryResponse getCategoryById(Long id);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}