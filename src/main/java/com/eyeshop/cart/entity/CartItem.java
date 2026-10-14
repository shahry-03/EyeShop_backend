package com.eyeshop.cart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "cart_items_cart_id_idx", columnList = "cart_id"),
    @Index(name = "cart_items_product_id_idx", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Snapshot of product name at time of adding (for display even if product renamed). */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    /** Unit price snapshot at time of adding. */
    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    public double getSubtotal() {
        return price * quantity;
    }
}