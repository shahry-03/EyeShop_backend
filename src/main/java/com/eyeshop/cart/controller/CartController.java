package com.eyeshop.cart.controller;

import com.eyeshop.auth.dto.response.ApiResponse;
import com.eyeshop.auth.entity.User;
import com.eyeshop.cart.dto.request.AddToCartRequest;
import com.eyeshop.cart.dto.request.UpdateCartItemRequest;
import com.eyeshop.cart.dto.response.CartResponse;
import com.eyeshop.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    /** Get current user's cart (auto-creates empty if missing). */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(ApiResponse.success(
            cartService.getCart(user.getId())));
    }

    /** Add product to cart. */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Item added to cart",
            cartService.addItem(user.getId(), request)));
    }

    /** Update quantity of specific item. */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Cart updated",
            cartService.updateItem(user.getId(), itemId, request)));
    }

    /** Remove specific item. */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId) {

        return ResponseEntity.ok(ApiResponse.success("Item removed",
            cartService.removeItem(user.getId(), itemId)));
    }

    /** Clear entire cart. */
    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(ApiResponse.success("Cart cleared",
            cartService.clearCart(user.getId())));
    }
}