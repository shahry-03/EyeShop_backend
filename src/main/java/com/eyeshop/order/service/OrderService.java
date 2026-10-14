package com.eyeshop.order.service;

import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.order.dto.request.OrderRequest;
import com.eyeshop.order.dto.request.OrderStatusUpdateRequest;
import com.eyeshop.order.dto.response.OrderResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderResponse placeOrder(UUID userId, OrderRequest request);

    OrderResponse getOrderById(Long orderId, UUID requestingUserId, boolean isAdmin);

    PageResponse<OrderResponse> getOrdersByUserId(UUID userId, Pageable pageable);

    OrderResponse cancelOrder(Long orderId, UUID requestingUserId, boolean isAdmin);

    OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request, UUID adminUserId);

    PageResponse<OrderResponse> getAllOrders(Pageable pageable);
}