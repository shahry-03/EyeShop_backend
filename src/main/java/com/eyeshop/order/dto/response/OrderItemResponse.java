package com.eyeshop.order.dto.response;

import com.eyeshop.order.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponse {
    private Long productId;
    private int quantity;
    private double price;

    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        OrderItemResponse orderItemResponse = new OrderItemResponse();
        orderItemResponse.setProductId(orderItem.getProductId());
        orderItemResponse.setQuantity(orderItem.getQuantity());
        orderItemResponse.setPrice(orderItem.getPrice());
        return orderItemResponse;
    }
}
