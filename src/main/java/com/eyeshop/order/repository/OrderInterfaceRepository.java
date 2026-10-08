package com.eyeshop.order.repository;

import com.eyeshop.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderInterfaceRepository extends JpaRepository<OrderItem, Long> {


}
