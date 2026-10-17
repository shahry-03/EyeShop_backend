package com.eyeshop.category.repository;

import com.eyeshop.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Sirf main categories fetch karne ke liye
    List<Category> findByParentIsNullOrderByNameAsc();
    
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByNameAndParent(String name, Category parent);
}