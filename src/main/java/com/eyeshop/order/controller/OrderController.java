package com.eyeshop.order.controller;

import com.eyeshop.auth.dto.response.ApiResponse;
import com.eyeshop.auth.entity.User;
import com.eyeshop.order.dto.request.OrderRequest;
import com.eyeshop.order.dto.response.OrderResponse;
import com.eyeshop.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.eyeshop.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.eyeshop.order.dto.request.CheckoutRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;

    // ─────────────────────────────────────────────
    //  PLACE ORDER (customer)
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderRequest request) {

        OrderResponse response = orderService.placeOrder(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Order placed successfully", response));
    }

    // ═══════════════════════════════════════════════════════════
    //  CHECKOUT — order from cart
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CheckoutRequest request) {

        OrderResponse response = orderService.checkout(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Checkout successful", response));
    }

    // ─────────────────────────────────────────────
    //  GET MY ORDERS
    // ─────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
            orderService.getOrdersByUserId(user.getId(), pageable)));
    }

    // ─────────────────────────────────────────────
    //  GET ORDER BY ID (own or admin)
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        boolean isAdmin = isAdmin(user);
        OrderResponse response = orderService.getOrderById(id, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─────────────────────────────────────────────
    //  CANCEL ORDER (own or admin)
    // ─────────────────────────────────────────────
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        boolean isAdmin = isAdmin(user);
        OrderResponse response = orderService.cancelOrder(id, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", response));
    }

    // ─────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────
    private boolean isAdmin(User user) {
        return user.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}