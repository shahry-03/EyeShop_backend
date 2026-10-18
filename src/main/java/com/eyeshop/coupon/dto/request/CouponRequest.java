package com.eyeshop.coupon.dto.request;

import com.eyeshop.coupon.entity.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record CouponRequest(
    @NotBlank(message = "Coupon code is required")
    String code,

    @NotNull(message = "Discount type is required")
    DiscountType discountType,

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    Double discountValue,

    Double minimumOrderAmount,
    Double maxDiscountAmount,
    LocalDateTime validFrom,
    LocalDateTime validUntil,
    Boolean active,
    Integer usageLimit
) {}