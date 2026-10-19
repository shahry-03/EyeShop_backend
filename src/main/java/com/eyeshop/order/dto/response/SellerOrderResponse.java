package com.eyeshop.order.dto.response;

import com.eyeshop.address.dto.response.AddressResponse;
import com.eyeshop.order.entity.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SellerOrderResponse(
    Long orderId,
    UUID buyerId,
    Double sellerTotalEarning, // Total earning of just this seller for this order
    Status orderStatus,
    LocalDateTime createdAt,
    AddressResponse shippingAddress,
    List<OrderItemResponse> myItems // Only the items sold by this seller
) {}