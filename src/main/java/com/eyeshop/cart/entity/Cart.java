package com.eyeshop.cart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts", indexes = {
    @Index(name = "carts_user_id_idx", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner — one cart per user (unique constraint). */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Helpers ─────────────────────────────────────────────

    /** Add item to cart (maintains bidirectional relationship). */
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    /** Remove item from cart. */
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    /**
     * Manually touch updatedAt — needed when only the items collection changes
     * (Hibernate's @UpdateTimestamp doesn't fire for collection-only changes).
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Total price = sum of all item subtotals. */
    public double getTotalPrice() {
        return items.stream()
            .mapToDouble(i -> i.getPrice() * i.getQuantity())
            .sum();
    }

    /** Total distinct items count. */
    public int getTotalItems() {
        return items.size();
    }


}