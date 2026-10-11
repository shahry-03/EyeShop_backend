package com.eyeshop.order.service.impl;

import com.eyeshop.auth.entity.User;
import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.auth.repositories.UserRepository;
import com.eyeshop.order.dto.request.OrderItemRequest;
import com.eyeshop.order.dto.request.OrderRequest;
import com.eyeshop.order.dto.request.OrderStatusUpdateRequest;
import com.eyeshop.order.dto.response.OrderResponse;
import com.eyeshop.order.entity.Order;
import com.eyeshop.order.entity.OrderItem;
import com.eyeshop.order.entity.OrderStatusHistory;
import com.eyeshop.order.entity.Status;
import com.eyeshop.order.exception.InsufficientStockException;
import com.eyeshop.order.exception.InvalidStatusTransitionException;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Allowed status transitions — the state machine.
     */
    private static final Map<Status, Set<Status>> ALLOWED_TRANSITIONS = Map.of(
        Status.PLACED,    Set.of(Status.CONFIRMED, Status.CANCELLED),
        Status.CONFIRMED, Set.of(Status.PACKED, Status.CANCELLED),
        Status.PACKED,    Set.of(Status.SHIPPED, Status.CANCELLED),
        Status.SHIPPED,   Set.of(Status.DELIVERED),
        Status.DELIVERED, Set.of(Status.RETURNED),
        Status.CANCELLED, Set.of(),
        Status.RETURNED,  Set.of()
    );

    // ═══════════════════════════════════════════════════════════
    //  PLACE ORDER
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public OrderResponse placeOrder(UUID userId, OrderRequest request) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (OrderItemRequest itemReq : request.itemsList()) {

            Product product = productRepository.findById(itemReq.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found: " + itemReq.productId()));

            if (product.getStock() < itemReq.quantity()) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for '%s'. Available: %d, requested: %d",
                        product.getName(), product.getStock(), itemReq.quantity()));
            }

            double lineTotal = product.getPrice() * itemReq.quantity();
            totalPrice += lineTotal;

            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .quantity(itemReq.quantity())
                .price(product.getPrice())
                .build();

            orderItems.add(item);
        }

        Order order = Order.builder()
            .userId(user.getId())
            .totalPrice(totalPrice)
            .status(Status.PLACED)
            .items(orderItems)
            .build();

        orderItems.forEach(item -> item.setOrder(order));

        // Add initial history entry
        OrderStatusHistory initialHistory = OrderStatusHistory.builder()
            .order(order)
            .fromStatus(null)
            .toStatus(Status.PLACED)
            .changedBy(user.getId())
            .note("Order placed")
            .build();
        order.getStatusHistory().add(initialHistory);

        Order saved = orderRepository.save(order);
        log.info("Order placed: id={}, userId={}, total={}", saved.getId(), userId, totalPrice);

        return OrderMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  GET ORDER BY ID
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, UUID requestingUserId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

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
    //  GET ALL ORDERS (admin)
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
            .stream()
            .map(OrderMapper::toResponse)
            .toList();
    }

    // ═══════════════════════════════════════════════════════════
    //  CANCEL ORDER (customer/admin)
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, UUID requestingUserId, boolean isAdmin) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!isAdmin && !order.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("You are not allowed to cancel this order");
        }

        if (order.getStatus() == Status.CANCELLED) {
            throw new InvalidStatusTransitionException("Order is already cancelled");
        }

        // Cancellable only in certain states
        if (!order.getStatus().isCancellable()) {
            throw new InvalidStatusTransitionException(
                String.format("Cannot cancel order in status %s. Only cancellable in: PLACED, CONFIRMED, PACKED",
                    order.getStatus()));
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found: " + item.getProductId()));
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        Status oldStatus = order.getStatus();
        order.setStatus(Status.CANCELLED);

        // Record history
        order.getStatusHistory().add(OrderStatusHistory.builder()
            .order(order)
            .fromStatus(oldStatus)
            .toStatus(Status.CANCELLED)
            .changedBy(requestingUserId)
            .note(isAdmin ? "Cancelled by admin" : "Cancelled by customer")
            .build());

        Order saved = orderRepository.save(order);
        log.info("Order cancelled: id={}, by={}, isAdmin={}", orderId, requestingUserId, isAdmin);

        return OrderMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  UPDATE STATUS (admin) — state machine enforced
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request, UUID adminUserId) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        Status current = order.getStatus();
        Status target = request.status();

        // Validate transition
        if (current == target) {
            throw new InvalidStatusTransitionException(
                "Order is already in status: " + current);
        }

        Set<Status> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidStatusTransitionException(
                String.format("Invalid transition: %s → %s. Allowed: %s",
                    current, target, allowed));
        }

        // Special case: cancelling via admin status update should restore stock
        if (target == Status.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + item.getProductId()));
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(target);

        // Record history
        order.getStatusHistory().add(OrderStatusHistory.builder()
            .order(order)
            .fromStatus(current)
            .toStatus(target)
            .changedBy(adminUserId)
            .note(request.note() != null ? request.note() : "Status changed by admin")
            .build());

        Order saved = orderRepository.save(order);
        log.info("Order status changed: id={}, {} → {}, by admin={}",
            orderId, current, target, adminUserId);

        return OrderMapper.toResponse(saved);
    }
}