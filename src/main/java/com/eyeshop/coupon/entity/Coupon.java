package com.eyeshop.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons", indexes = {
    @Index(name = "coupons_code_idx", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // e.g., "EID50", "WELCOME10"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false)
    private Double discountValue;

    /** Minimum cart total required to apply this coupon */
    @Column(name = "min_order_amount")
    @Builder.Default
    private Double minimumOrderAmount = 0.0;

    /** Max discount allowed (especially for Percentage). e.g., 10% off up to Rs. 1000 */
    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "usage_limit")
    private Integer usageLimit; // null = unlimited

    @Column(name = "times_used", nullable = false)
    @Builder.Default
    private int timesUsed = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}