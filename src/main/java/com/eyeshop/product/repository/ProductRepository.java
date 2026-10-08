package com.eyeshop.product.repository;

import com.eyeshop.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ----Custom Methods----
    // fetch product by name
    Optional<Product> findByName(String name);

    // find products by category
    List<Product> findByCategory(String category);

    // find product by name (for duplicate check)
    boolean existsByName(String name);
}
