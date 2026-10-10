package com.eyeshop.order.mapper;

import com.eyeshop.order.dto.response.OrderItemResponse;
import com.eyeshop.order.dto.response.OrderResponse;
import com.eyeshop.order.entity.Order;
import com.eyeshop.order.entity.OrderItem;

import java.util.List;

/**
 * Manual mapper for Order entity ↔ DTOs.
 * Static methods — no Spring bean needed, no dependencies.
 */
public final class OrderMapper {

    private OrderMapper() {
        // utility class — no instances
    }

    public static OrderResponse toResponse(Order order) {
        if (order == null) return null;
        return new OrderResponse(
            order.getId(),
            order.getUserId(),
            order.getTotalPrice(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            toItemResponseList(order.getItems())
        );
    }

    public static OrderItemResponse toItemResponse(OrderItem item) {
        if (item == null) return null;
        return new OrderItemResponse(
            item.getProductId(),
            item.getQuantity(),
            item.getPrice()
        );
    }

    private static List<OrderItemResponse> toItemResponseList(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return List.of();
        return items.stream()
            .map(OrderMapper::toItemResponse)
            .toList();
    }
}