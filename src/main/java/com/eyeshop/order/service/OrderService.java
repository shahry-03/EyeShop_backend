package com.eyeshop.order.service;

import com.eyeshop.order.dto.request.OrderRequest;
import com.eyeshop.order.dto.request.OrderStatusUpdateRequest;
import com.eyeshop.order.dto.response.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse placeOrder(UUID userId, OrderRequest request);

    OrderResponse getOrderById(Long orderId, UUID requestingUserId, boolean isAdmin);

    List<OrderResponse> getOrdersByUserId(UUID userId);

    OrderResponse cancelOrder(Long orderId, UUID requestingUserId, boolean isAdmin);

    OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request, UUID adminUserId);

    List<OrderResponse> getAllOrders();
}