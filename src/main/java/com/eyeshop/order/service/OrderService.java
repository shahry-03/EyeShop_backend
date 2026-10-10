package com.eyeshop.order.service;

import com.eyeshop.order.dto.request.OrderRequest;
import com.eyeshop.order.dto.response.OrderResponse;

import java.util.List;
import java.util.UUID;

/**
 * Order service — handles placing, viewing, and cancelling orders.
 *
 * Security note:
 *   - Place order uses the authenticated user's id (never from client).
 *   - View/cancel methods accept a flag indicating whether the caller is admin,
 *     so the service can enforce ownership rules.
 */
public interface OrderService {

    /**
     * Place a new order for the given user.
     * @param userId the id of the authenticated user (from JWT)
     * @param request the order payload (items only — no userId)
     */
    OrderResponse placeOrder(UUID userId, OrderRequest request);

    /**
     * Get order by id.
     * Enforces ownership: non-admin users can only see their own orders.
     */
    OrderResponse getOrderById(Long orderId, UUID requestingUserId, boolean isAdmin);

    /**
     * List all orders for a user.
     */
    List<OrderResponse> getOrdersByUserId(UUID userId);

    /**
     * Cancel an order.
     * Enforces ownership: non-admin users can only cancel their own orders.
     */
    OrderResponse cancelOrder(Long orderId, UUID requestingUserId, boolean isAdmin);
}