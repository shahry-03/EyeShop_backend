package com.eyeshop.order.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Checkout request — items come from the user's active cart.
 * Only shipping address needs to be provided.
 */
public record CheckoutRequest(

    @NotNull(message = "Shipping address is required")
    Long shippingAddressId,

    String couponCode // Optional (null/blank = no coupon)
) {}