package com.eyeshop.order.dto.response;

import com.eyeshop.order.entity.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusHistoryResponse(
    Long id,
    Status fromStatus,
    Status toStatus,
    UUID changedBy,
    String note,
    LocalDateTime changedAt
) {}