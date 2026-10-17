package com.eyeshop.product.mapper;

import com.eyeshop.product.dto.response.ProductImageResponse;
import com.eyeshop.product.dto.response.ProductResponse;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.entity.ProductImage;
import com.eyeshop.category.mapper.CategoryMapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductResponse toResponse(Product product) {
        if (product == null) return null;
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategory() != null ? CategoryMapper.toResponse(product.getCategory(), false) : null,
            toImageResponseList(product.getImages()),
            product.getAverageRating(),   
            product.getTotalReviews(),    
            product.getCreatedAt()
        );
    }

    public static ProductImageResponse toImageResponse(ProductImage image) {
        if (image == null) return null;
        return new ProductImageResponse(
            image.getId(),
            image.getUrl(),
            image.getPublicId(),
            image.isPrimary(),
            image.getDisplayOrder()
        );
    }

    private static List<ProductImageResponse> toImageResponseList(List<ProductImage> images) {
        if (images == null || images.isEmpty()) return Collections.emptyList();
        return images.stream().map(ProductMapper::toImageResponse).toList();
    }

    public static List<ProductResponse> toResponseList(List<Product> products) {
        if (products == null || products.isEmpty()) return Collections.emptyList();
        return products.stream().map(ProductMapper::toResponse).collect(Collectors.toList());
    }
}