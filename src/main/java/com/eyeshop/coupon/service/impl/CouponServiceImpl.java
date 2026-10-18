package com.eyeshop.coupon.service.impl;

import com.eyeshop.coupon.dto.request.CouponRequest;
import com.eyeshop.coupon.dto.response.CouponResponse;
import com.eyeshop.coupon.entity.Coupon;
import com.eyeshop.coupon.entity.DiscountType;
import com.eyeshop.coupon.exception.InvalidCouponException;
import com.eyeshop.coupon.mapper.CouponMapper;
import com.eyeshop.coupon.repository.CouponRepository;
import com.eyeshop.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCodeIgnoreCase(request.code())) {
            throw new InvalidCouponException("Coupon code already exists: " + request.code());
        }
        
        Coupon coupon = Coupon.builder()
            .code(request.code().toUpperCase())
            .discountType(request.discountType())
            .discountValue(request.discountValue())
            .minimumOrderAmount(request.minimumOrderAmount() != null ? request.minimumOrderAmount() : 0.0)
            .maxDiscountAmount(request.maxDiscountAmount())
            .validFrom(request.validFrom())
            .validUntil(request.validUntil())
            .active(request.active() != null ? request.active() : true)
            .usageLimit(request.usageLimit())
            .build();

        return CouponMapper.toResponse(couponRepository.saveAndFlush(coupon));
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new InvalidCouponException("Coupon not found: " + id));

        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinimumOrderAmount(request.minimumOrderAmount());
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
        coupon.setValidFrom(request.validFrom());
        coupon.setValidUntil(request.validUntil());
        if (request.active() != null) coupon.setActive(request.active());
        coupon.setUsageLimit(request.usageLimit());

        return CouponMapper.toResponse(couponRepository.saveAndFlush(coupon));
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public double validateAndCalculateDiscount(String code, double cartTotal) {
        Coupon coupon = getCouponByCode(code);
        LocalDateTime now = LocalDateTime.now();

        // 1. Check active status
        if (!coupon.isActive()) throw new InvalidCouponException("Coupon is inactive.");

        // 2. Check valid dates
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            throw new InvalidCouponException("Coupon is not yet valid.");
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            throw new InvalidCouponException("Coupon has expired.");
        }

        // 3. Check usage limit
        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            throw new InvalidCouponException("Coupon usage limit reached.");
        }

        // 4. Check minimum order amount
        if (cartTotal < coupon.getMinimumOrderAmount()) {
            throw new InvalidCouponException("Minimum order amount for this coupon is Rs. " + coupon.getMinimumOrderAmount());
        }

        // 5. Calculate discount
        double discount = 0.0;
        if (coupon.getDiscountType() == DiscountType.FLAT) {
            discount = coupon.getDiscountValue();
        } else if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = cartTotal * (coupon.getDiscountValue() / 100.0);
            
            // Apply max discount cap if present
            if (coupon.getMaxDiscountAmount() != null && discount > coupon.getMaxDiscountAmount()) {
                discount = coupon.getMaxDiscountAmount();
            }
        }

        // Discount cannot exceed cart total
        return Math.min(discount, cartTotal);
    }

    @Override
    @Transactional
    public void recordUsage(Coupon coupon) {
        coupon.setTimesUsed(coupon.getTimesUsed() + 1);
        couponRepository.save(coupon);
        log.info("Coupon used: code={}, totalUsed={}", coupon.getCode(), coupon.getTimesUsed());
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code)
            .orElseThrow(() -> new InvalidCouponException("Invalid coupon code: " + code));
    }
}