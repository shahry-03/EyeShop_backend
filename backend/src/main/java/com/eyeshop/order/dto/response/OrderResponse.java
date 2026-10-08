package com.eyeshop.order.dto.response;

import java.util.UUID;
import com.eyeshop.order.entity.Order;
import com.eyeshop.order.entity.OrderItem;
import com.eyeshop.order.entity.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {

    private Long id;

    private UUID userId;

    private Double totalPrice;

    private Status status;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;

    public static OrderResponse fromEntity(Order order){
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setUserId(order.getUserId());
        orderResponse.setTotalPrice(order.getTotalPrice());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setCreatedAt(order.getCreatedAt());
        orderResponse.setItems(order.getItems()
                .stream()
                .map(OrderItemResponse::fromEntity)
                .toList());
        return orderResponse;
    }

}
