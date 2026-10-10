package com.eyeshop.order.dto.response;

import com.eyeshop.order.entity.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    Long id,
    UUID userId,
    Double totalPrice,
    Status status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<OrderItemResponse> items
) {}