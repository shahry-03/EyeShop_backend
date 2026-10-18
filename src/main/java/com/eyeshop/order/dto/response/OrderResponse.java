package com.eyeshop.order.dto.response;

import com.eyeshop.address.dto.response.AddressResponse;
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
    AddressResponse shippingAddress,
    Double discountAmount,
    String couponCode,                                 
    List<OrderItemResponse> items,
    List<OrderStatusHistoryResponse> statusHistory
) {}