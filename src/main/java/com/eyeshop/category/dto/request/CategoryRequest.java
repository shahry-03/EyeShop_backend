package com.eyeshop.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    String name,

    @Size(max = 500)
    String description,

    Long parentId // Agar null hai, toh root category banegi
) {}