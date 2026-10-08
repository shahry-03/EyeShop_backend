package com.eyeshop.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateRequest {

    @NotBlank(message = "Name cannot be empty")
    private String name;
    private String description;

    @Positive(message = "Price must be greater than zero")
    private double price;

    @NotBlank(message = "Category cannot be empty")
    private String category;

}
