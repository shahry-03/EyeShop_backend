package com.eyeshop.order.dto.request;

import com.eyeshop.order.entity.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderStatusUpdateRequest(

    @NotNull(message = "Target status is required")
    Status status,

    @Size(max = 500, message = "Note must not exceed 500 characters")
    String note
) {}