package com.eyeshop.product.mapper;

import com.eyeshop.product.dto.response.ProductResponse;
import com.eyeshop.product.entity.Product;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual mapper for Product entity ↔ DTOs.
 * Static methods — no Spring bean needed, no dependencies.
 */
public final class ProductMapper {

    private ProductMapper() {
        // utility class — no instances
    }

    public static ProductResponse toResponse(Product product) {
        if (product == null) return null;
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategory(),
            product.getCreatedAt()
        );
    }

    public static List<ProductResponse> toResponseList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
            .map(ProductMapper::toResponse)
            .collect(Collectors.toList());
    }
}