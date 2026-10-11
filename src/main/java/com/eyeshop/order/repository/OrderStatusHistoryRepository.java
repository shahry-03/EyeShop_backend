package com.eyeshop.order.repository;

import com.eyeshop.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    // Standard JPA enough — history fetched via Order relation.
}