package com.eyeshop.order.entity;

public enum Status {

    PLACED,        // Customer placed the order
    CONFIRMED,     // Admin confirmed
    PACKED,        // Items packed
    SHIPPED,       // Handed over to courier
    DELIVERED,     // Delivered to customer
    CANCELLED,     // Cancelled before delivery
    RETURNED;      // Returned after delivery

    /**
     * Terminal states — no further transitions possible.
     */
    public boolean isTerminal() {
        return this == CANCELLED || this == RETURNED;
    }

    /**
     * Cancellable states — customer/admin can cancel here.
     */
    public boolean isCancellable() {
        return this == PLACED || this == CONFIRMED || this == PACKED;
    }
}