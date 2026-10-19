package com.eyeshop.order.mapper;

import com.eyeshop.address.dto.response.AddressResponse;
import com.eyeshop.address.entity.Address;
import com.eyeshop.address.mapper.AddressMapper;
import com.eyeshop.order.dto.response.OrderItemResponse;
import com.eyeshop.order.dto.response.OrderResponse;
import com.eyeshop.order.dto.response.OrderStatusHistoryResponse;
import com.eyeshop.order.entity.Order;
import com.eyeshop.order.entity.OrderItem;
import com.eyeshop.order.entity.OrderStatusHistory;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {}

    /**
     * Map order → response. Shipping address object needs to be passed in
     * (avoid N+1 queries by fetching it separately).
     */
    public static OrderResponse toResponse(Order order, Address shippingAddress) {
        if (order == null) return null;
        return new OrderResponse(
            order.getId(),
            order.getUserId(),
            order.getTotalPrice(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            AddressMapper.toResponse(shippingAddress),
            order.getDiscountAmount(), 
            order.getCouponCode(),
            toItemResponseList(order.getItems()),
            toHistoryResponseList(order.getStatusHistory())
        );
    }

    public static OrderItemResponse toItemResponse(OrderItem item) {
        if (item == null) return null;
        return new OrderItemResponse(
            item.getProductId(),
            item.getQuantity(),
            item.getPrice(),
            item.getSellerId(),
            item.getSellerEarning(),
            item.getPlatformEarning()
        );
    }

    public static OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory h) {
        if (h == null) return null;
        return new OrderStatusHistoryResponse(
            h.getId(), h.getFromStatus(), h.getToStatus(),
            h.getChangedBy(), h.getNote(), h.getChangedAt());
    }

    private static List<OrderItemResponse> toItemResponseList(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return List.of();
        return items.stream().map(OrderMapper::toItemResponse).toList();
    }

    private static List<OrderStatusHistoryResponse> toHistoryResponseList(
            List<OrderStatusHistory> history) {
        if (history == null || history.isEmpty()) return List.of();
        return history.stream().map(OrderMapper::toHistoryResponse).toList();
    }
}