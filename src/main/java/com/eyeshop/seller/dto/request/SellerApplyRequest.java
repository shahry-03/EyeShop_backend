package com.eyeshop.seller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SellerApplyRequest(
    @NotBlank(message = "Shop name is required")
    String shopName,
    String shopDescription
) {}