package com.eyeshop.coupon.mapper;

import com.eyeshop.coupon.dto.response.CouponResponse;
import com.eyeshop.coupon.entity.Coupon;

public final class CouponMapper {
    private CouponMapper() {}

    public static CouponResponse toResponse(Coupon c) {
        if (c == null) return null;
        return new CouponResponse(
            c.getId(), c.getCode(), c.getDiscountType(), c.getDiscountValue(),
            c.getMinimumOrderAmount(), c.getMaxDiscountAmount(),
            c.getValidFrom(), c.getValidUntil(), c.isActive(),
            c.getUsageLimit(), c.getTimesUsed(), c.getCreatedAt()
        );
    }
}