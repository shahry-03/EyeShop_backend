package com.eyeshop.seller.dto.response;

import java.time.LocalDateTime;

public record SellerResponse(
    Long id,
    String shopName,
    String shopDescription,
    boolean approved,
    Double commissionRate,
    Double walletBalance,
    LocalDateTime createdAt
) {}