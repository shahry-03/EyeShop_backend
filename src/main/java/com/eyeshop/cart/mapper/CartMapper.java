package com.eyeshop.cart.mapper;

import com.eyeshop.cart.dto.response.CartItemResponse;
import com.eyeshop.cart.dto.response.CartResponse;
import com.eyeshop.cart.entity.Cart;
import com.eyeshop.cart.entity.CartItem;

import java.util.List;

public final class CartMapper {

    private CartMapper() {}

    public static CartResponse toResponse(Cart cart) {
        if (cart == null) return null;
        return new CartResponse(
            cart.getId(),
            cart.getUserId(),
            toItemResponseList(cart.getItems()),
            cart.getTotalItems(),
            cart.getTotalPrice(),
            cart.getCreatedAt(),
            cart.getUpdatedAt()
        );
    }

    public static CartItemResponse toItemResponse(CartItem item) {
        if (item == null) return null;
        return new CartItemResponse(
            item.getId(),
            item.getProductId(),
            item.getProductName(),
            item.getPrice(),
            item.getQuantity(),
            item.getSubtotal()
        );
    }

    private static List<CartItemResponse> toItemResponseList(List<CartItem> items) {
        if (items == null || items.isEmpty()) return List.of();
        return items.stream().map(CartMapper::toItemResponse).toList();
    }
}