package com.eyeshop.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;


    @Column(nullable = false)
    private Double price;

    /** Snapshot of seller who owns this product (null if admin's own product) */
    @Column(name = "seller_id")
    private Long sellerId;

    /** Snapshot of commission rate at time of order (e.g., 10.0) */
    @Column(name = "commission_rate")
    private Double commissionRate;

    /** Actual amount credited to seller (price * quantity * (1 - commission/100)) */
    @Column(name = "seller_earning")
    private Double sellerEarning;

    /** Actual amount platform keeps (price * quantity * commission/100) */
    @Column(name = "platform_earning")
    private Double platformEarning;

}
