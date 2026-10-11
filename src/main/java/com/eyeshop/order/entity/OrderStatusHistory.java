package com.eyeshop.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit trail — records every status change of an order.
 * Enables full order timeline for customer + admin.
 */
@Entity
@Table(name = "order_status_history", indexes = {
    @Index(name = "order_status_history_order_id_idx", columnList = "order_id"),
    @Index(name = "order_status_history_changed_at_idx", columnList = "changed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Previous status — null for the initial PLACED entry. */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private Status fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private Status toStatus;

    /** User who made the change (customer or admin). */
    @Column(name = "changed_by")
    private UUID changedBy;

    /** Optional note — e.g. "Shipped via TCS, tracking #12345". */
    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;
}