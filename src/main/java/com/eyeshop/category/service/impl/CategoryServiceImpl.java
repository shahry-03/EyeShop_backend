package com.eyeshop.category.service.impl;

import com.eyeshop.category.dto.request.CategoryRequest;
import com.eyeshop.category.dto.response.CategoryResponse;
import com.eyeshop.category.entity.Category;
import com.eyeshop.category.mapper.CategoryMapper;
import com.eyeshop.category.repository.CategoryRepository;
import com.eyeshop.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category parent = null;
        if (request.parentId() != null) {
            parent = categoryRepository.findById(request.parentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
                
            if (categoryRepository.existsByNameAndParent(request.name(), parent)) {
                throw new IllegalArgumentException("Category already exists under this parent");
            }
        }

        String slug = generateSlug(request.name());
        // Ensure unique slug
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Category category = Category.builder()
            .name(request.name())
            .slug(slug)
            .description(request.description())
            .parent(parent)
            .build();

        Category saved = categoryRepository.saveAndFlush(category);
        log.info("Category created: id={}, name={}, parent={}", saved.getId(), saved.getName(), parent != null ? parent.getId() : "ROOT");
        
        return CategoryMapper.toResponse(saved, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        // Fetch only root categories, JPA will auto-load children due to mapping
        List<Category> roots = categoryRepository.findByParentIsNullOrderByNameAsc();
        return CategoryMapper.toResponseList(roots, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllFlat() {
        return CategoryMapper.toResponseList(categoryRepository.findAll(), false);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        return CategoryMapper.toResponse(category, true);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        if (request.parentId() != null && request.parentId().equals(id)) {
            throw new IllegalArgumentException("A category cannot be its own parent");
        }

        Category parent = request.parentId() != null 
            ? categoryRepository.findById(request.parentId()).orElseThrow(() -> new IllegalArgumentException("Parent not found"))
            : null;

        category.setName(request.name());
        category.setDescription(request.description());
        category.setParent(parent);

        return CategoryMapper.toResponse(categoryRepository.saveAndFlush(category), false);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        
        // Note: Agar is category ke andar products ya sub-categories hain, toh unhe pehle handle karna padega.
        // Abhi ke liye direct delete kar rahe hain.
        categoryRepository.delete(category);
        log.info("Category deleted: id={}", id);
    }

    // Helper to create URL-friendly slug
    private String generateSlug(String name) {
        String nowhitespace = Pattern.compile("[\\s]").matcher(name).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}