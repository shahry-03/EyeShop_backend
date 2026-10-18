package com.eyeshop.coupon.service;

import com.eyeshop.coupon.dto.request.CouponRequest;
import com.eyeshop.coupon.dto.response.CouponResponse;
import com.eyeshop.coupon.entity.Coupon;

public interface CouponService {
    CouponResponse createCoupon(CouponRequest request);
    CouponResponse updateCoupon(Long id, CouponRequest request);
    void deleteCoupon(Long id);
    
    /**
     * Validate coupon and calculate discount amount.
     * Returns the actual discount to be applied on the cart total.
     */
    double validateAndCalculateDiscount(String code, double cartTotal);
    
    /** Increment usage after successful order */
    void recordUsage(Coupon coupon);
    
    Coupon getCouponByCode(String code);
}