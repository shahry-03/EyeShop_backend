package com.eyeshop.order.repository;

import com.eyeshop.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(UUID userId);
    Page<Order> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i " +
           "WHERE o.userId = :userId AND i.productId = :productId AND o.status = 'DELIVERED'")
    boolean hasUserPurchasedProduct(@Param("userId") UUID userId, @Param("productId") Long productId);

    // ═══ NEW: Seller Dashboard Queries ═══

    // 1. Get distinct orders containing this seller's items
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.sellerId = :sellerId")
    Page<Order> findOrdersBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // 2. Total Earnings (Only from DELIVERED orders)
    @Query("SELECT COALESCE(SUM(i.sellerEarning), 0.0) FROM OrderItem i " +
           "WHERE i.sellerId = :sellerId AND i.order.status = 'DELIVERED'")
    Double getTotalSalesBySellerId(@Param("sellerId") Long sellerId);

    // 3. Total Orders count
    @Query("SELECT COUNT(DISTINCT i.order.id) FROM OrderItem i WHERE i.sellerId = :sellerId")
    Long getTotalOrdersBySellerId(@Param("sellerId") Long sellerId);

    // 4. Pending Orders count (Not Delivered, Not Cancelled)
    @Query("SELECT COUNT(DISTINCT i.order.id) FROM OrderItem i " +
           "WHERE i.sellerId = :sellerId AND i.order.status NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED')")
    Long getPendingOrdersBySellerId(@Param("sellerId") Long sellerId);
}