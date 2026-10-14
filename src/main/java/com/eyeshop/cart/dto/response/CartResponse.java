package com.eyeshop.cart.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartResponse(
    Long id,
    UUID userId,
    List<CartItemResponse> items,
    int totalItems,
    Double totalPrice,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}