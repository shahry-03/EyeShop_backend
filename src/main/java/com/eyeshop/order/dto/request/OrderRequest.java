package com.eyeshop.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(

    @NotNull(message = "Shipping address is required")
    Long shippingAddressId,                                          

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    List<OrderItemRequest> itemsList
) {}