package com.eyeshop.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    String name,

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description,

    @Positive(message = "Price must be greater than zero")
    double price,

    @NotBlank(message = "Category cannot be empty")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    String category
) {}