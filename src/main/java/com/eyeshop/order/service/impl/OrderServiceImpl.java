package com.eyeshop.order.service.impl;

import com.eyeshop.auth.entity.User;
import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.auth.repositories.UserRepository;
import com.eyeshop.order.dto.request.OrderItemRequest;
import com.eyeshop.order.dto.request.OrderRequest;
import com.eyeshop.order.dto.response.OrderResponse;
import com.eyeshop.order.entity.Order;
import com.eyeshop.order.entity.OrderItem;
import com.eyeshop.order.entity.Status;
import com.eyeshop.order.exception.InsufficientStockException;
import com.eyeshop.order.mapper.OrderMapper;
import com.eyeshop.order.repository.OrderRepository;
import com.eyeshop.order.service.OrderService;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ═══════════════════════════════════════════════════════════
    //  PLACE ORDER
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public OrderResponse placeOrder(UUID userId, OrderRequest request) {

        // 1. Verify user exists (JWT already authenticated, but be safe)
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 2. Build order items + calculate total + deduct stock
        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (OrderItemRequest itemReq : request.itemsList()) {

            Product product = productRepository.findById(itemReq.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found: " + itemReq.productId()));

            // Stock check
            if (product.getStock() < itemReq.quantity()) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for '%s'. Available: %d, requested: %d",
                        product.getName(), product.getStock(), itemReq.quantity()));
            }

            // Line total = unit price × quantity
            double lineTotal = product.getPrice() * itemReq.quantity();
            totalPrice += lineTotal;

            // Deduct stock (optimistic lock via @Version on Product handles races)
            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            // Snapshot the unit price at time of purchase
            OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .quantity(itemReq.quantity())
                .price(product.getPrice())   // ← unit price
                .build();

            orderItems.add(item);
        }

        // 3. Build order
        Order order = Order.builder()
            .userId(user.getId())
            .totalPrice(totalPrice)
            .status(Status.PLACED)
            .items(orderItems)
            .build();

        // 4. Link items → order (for cascade)
        orderItems.forEach(item -> item.setOrder(order));

        // 5. Save
        Order saved = orderRepository.save(order);
        log.info("Order placed: id={}, userId={}, total={}", saved.getId(), userId, totalPrice);

        return OrderMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  GET ORDER BY ID (with ownership check)
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, UUID requestingUserId, boolean isAdmin) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        // Ownership check — non-admin can only see own orders
        if (!isAdmin && !order.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("You are not allowed to view this order");
        }

        return OrderMapper.toResponse(order);
    }

    // ═══════════════════════════════════════════════════════════
    //  GET ORDERS BY USER
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId)
            .stream()
            .map(OrderMapper::toResponse)
            .toList();
    }

    // ═══════════════════════════════════════════════════════════
    //  CANCEL ORDER (with ownership check)
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, UUID requestingUserId, boolean isAdmin) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        // Ownership check
        if (!isAdmin && !order.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("You are not allowed to cancel this order");
        }

        // Already cancelled?
        if (order.getStatus() == Status.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found: " + item.getProductId()));

            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Status.CANCELLED);
        Order saved = orderRepository.save(order);

        log.info("Order cancelled: id={}, by user={}, isAdmin={}",
            orderId, requestingUserId, isAdmin);

        return OrderMapper.toResponse(saved);
    }
}