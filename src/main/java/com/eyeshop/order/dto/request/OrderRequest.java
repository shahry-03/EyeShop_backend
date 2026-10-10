package com.eyeshop.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Place-order request.
 * NOTE: userId is NOT here — it comes from the JWT.
 */
public record OrderRequest(

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    List<OrderItemRequest> itemsList
) {}