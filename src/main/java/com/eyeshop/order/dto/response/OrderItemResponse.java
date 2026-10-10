package com.eyeshop.order.dto.response;

public record OrderItemResponse(
    Long productId,
    int quantity,
    double price
) {}