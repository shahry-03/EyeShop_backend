package com.eyeshop.coupon.dto.response;

import com.eyeshop.coupon.entity.DiscountType;
import java.time.LocalDateTime;

public record CouponResponse(
    Long id,
    String code,
    DiscountType discountType,
    Double discountValue,
    Double minimumOrderAmount,
    Double maxDiscountAmount,
    LocalDateTime validFrom,
    LocalDateTime validUntil,
    boolean active,
    Integer usageLimit,
    int timesUsed,
    LocalDateTime createdAt
) {}