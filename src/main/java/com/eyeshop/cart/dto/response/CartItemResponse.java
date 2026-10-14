package com.eyeshop.cart.dto.response;

public record CartItemResponse(
    Long id,
    Long productId,
    String productName,
    Double price,
    int quantity,
    Double subtotal
) {}