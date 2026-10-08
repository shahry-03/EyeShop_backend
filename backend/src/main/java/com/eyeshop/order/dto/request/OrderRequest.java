package com.eyeshop.order.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotNull(message = "User id cannot be null")
    private UUID userId;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> itemsList;
}
