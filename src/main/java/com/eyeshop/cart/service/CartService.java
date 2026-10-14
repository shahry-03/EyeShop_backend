package com.eyeshop.cart.service;

import com.eyeshop.cart.dto.request.AddToCartRequest;
import com.eyeshop.cart.dto.request.UpdateCartItemRequest;
import com.eyeshop.cart.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {

    /** Get user's cart (auto-creates empty one if missing). */
    CartResponse getCart(UUID userId);

    /** Add product to cart. If already present, quantity increments. */
    CartResponse addItem(UUID userId, AddToCartRequest request);

    /** Update quantity of a specific cart item. */
    CartResponse updateItem(UUID userId, Long itemId, UpdateCartItemRequest request);

    /** Remove a specific item from cart. */
    CartResponse removeItem(UUID userId, Long itemId);

    /** Clear all items from cart. */
    CartResponse clearCart(UUID userId);

    /**
     * Empty the cart after successful order placement.
     * Internal use — called from OrderService.
     */
    void emptyCartForUser(UUID userId);
}