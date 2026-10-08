package com.eyeshop.product.dto.response;

import com.eyeshop.product.entity.Product;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private LocalDateTime createdAt;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(product.getId());
        productResponse.setName(product.getName());
        productResponse.setDescription(product.getDescription());
        productResponse.setPrice(product.getPrice());
        productResponse.setStock(product.getStock());
        productResponse.setCategory(product.getCategory());
        productResponse.setCreatedAt(product.getCreatedAt());
        return productResponse;

    }
}
