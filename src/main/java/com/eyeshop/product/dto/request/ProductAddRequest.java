package com.eyeshop.product.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductAddRequest {

    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    @Positive(message = "Price must be greater than zero")
    private double price;

    @Min(value = 0, message = "Stock must be greater than or equal to zero")
    private int stock;

    @NotBlank(message = "Category cannot be empty")
    private String category;
}
