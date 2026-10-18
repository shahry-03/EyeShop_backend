package com.eyeshop.order.service.impl;

import com.eyeshop.address.entity.Address;
import com.eyeshop.address.exception.AddressNotFoundException;
import com.eyeshop.address.repository.AddressRepository;
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
import com.eyeshop.common.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.eyeshop.cart.entity.Cart;
import com.eyeshop.cart.entity.CartItem;
import com.eyeshop.cart.exception.CartNotFoundException;
import com.eyeshop.cart.repository.CartRepository;
import com.eyeshop.cart.service.CartService;
import com.eyeshop.order.dto.request.CheckoutRequest;
import com.eyeshop.order.exception.EmptyCartException;
import com.eyeshop.coupon.service.CouponService;
import com.eyeshop.coupon.repository.CouponRepository;
import com.eyeshop.coupon.entity.Coupon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final CouponService couponService;
    private final CouponRepository couponRepository;

    /**
     * Allowed status transitions — the state machine.
     * Immutable map — thread-safe, defined once.
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

    // ═══════════════════════════════════════════════════════════════
    //  PLACE ORDER
    // ═══════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public OrderResponse placeOrder(UUID userId, OrderRequest request) {

        // 1. Verify user (JWT already authenticated, but defence in depth)
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 2. Validate shipping address ownership
        Address shippingAddress = addressRepository
            .findByIdAndUserId(request.shippingAddressId(), userId)
            .orElseThrow(() -> new AddressNotFoundException(
                "Shipping address not found or does not belong to you: "
                    + request.shippingAddressId()));

        // 3. Build order items + calculate total + deduct stock
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

            // Deduct stock — optimistic lock via @Version handles races
            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .quantity(itemReq.quantity())
                .price(product.getPrice())
                .build();

            orderItems.add(item);
        }

        // 4. Build order
        Order order = Order.builder()
            .userId(user.getId())
            .shippingAddressId(shippingAddress.getId())
            .totalPrice(totalPrice)
            .status(Status.PLACED)
            .items(orderItems)
            .build();

        orderItems.forEach(item -> item.setOrder(order));

        // 5. Record initial history
        order.getStatusHistory().add(OrderStatusHistory.builder()
            .order(order)
            .fromStatus(null)
            .toStatus(Status.PLACED)
            .changedBy(user.getId())
            .note("Order placed")
            .build());

        // 6. Persist
        Order saved = orderRepository.save(order);

        log.info("Order placed: id={}, userId={}, total={}, shippingAddressId={}",
            saved.getId(), userId, totalPrice, shippingAddress.getId());

        return OrderMapper.toResponse(saved, shippingAddress);
    }

    // ═══════════════════════════════════════════════════════════════
    //  GET ORDER BY ID
    // ═══════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, UUID requestingUserId, boolean isAdmin) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!isAdmin && !order.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("You are not allowed to view this order");
        }

        Address shippingAddress = fetchShippingAddress(order);
        return OrderMapper.toResponse(order, shippingAddress);
    }

    // ═══════════════════════════════════════════════════════════
    //  GET ORDERS BY USER (paginated)
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByUserId(UUID userId, Pageable pageable) {
        Page<Order> page = orderRepository.findByUserId(userId, pageable);
        List<OrderResponse> content = mapOrdersWithAddresses(page.getContent());
        return PageResponse.of(content, page);
    }

    // ═══════════════════════════════════════════════════════════
    //  GET ALL ORDERS (admin, paginated)
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> page = orderRepository.findAll(pageable);
        List<OrderResponse> content = mapOrdersWithAddresses(page.getContent());
        return PageResponse.of(content, page);
    }


    // ═══════════════════════════════════════════════════════════════
    //  CANCEL ORDER (customer/admin)
    // ═══════════════════════════════════════════════════════════════

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
            throw new InvalidStatusTransitionException("Order is already cancelled");
        }

        // Cancellable only in specific states
        if (!order.getStatus().isCancellable()) {
            throw new InvalidStatusTransitionException(
                String.format("Cannot cancel order in status %s. Only cancellable in: PLACED, CONFIRMED, PACKED",
                    order.getStatus()));
        }

        // Restore stock
        restoreStockForItems(order);

        // Update status
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

        return OrderMapper.toResponse(saved, fetchShippingAddress(saved));
    }

    // ═══════════════════════════════════════════════════════════════
    //  UPDATE STATUS (admin) — state machine enforced
    // ═══════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request, UUID adminUserId) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        Status current = order.getStatus();
        Status target = request.status();

        // No-op check
        if (current == target) {
            throw new InvalidStatusTransitionException(
                "Order is already in status: " + current);
        }

        // Validate transition against state machine
        Set<Status> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidStatusTransitionException(
                String.format("Invalid transition: %s → %s. Allowed: %s",
                    current, target, allowed));
        }

        // Special case: transition to CANCELLED via admin status update → restore stock
        if (target == Status.CANCELLED) {
            restoreStockForItems(order);
        }

        order.setStatus(target);

        // Record history
        order.getStatusHistory().add(OrderStatusHistory.builder()
            .order(order)
            .fromStatus(current)
            .toStatus(target)
            .changedBy(adminUserId)
            .note(request.note() != null && !request.note().isBlank()
                ? request.note()
                : "Status changed by admin")
            .build());

        Order saved = orderRepository.save(order);
        log.info("Order status changed: id={}, {} → {}, by admin={}",
            orderId, current, target, adminUserId);

        return OrderMapper.toResponse(saved, fetchShippingAddress(saved));
    }


    // ═══════════════════════════════════════════════════════════
    //  CHECKOUT — build order from user's active cart
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public OrderResponse checkout(UUID userId, CheckoutRequest request) {

        // 1. Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 2. Load user's cart — must exist and be non-empty
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new CartNotFoundException(
                "Cart not found for user: " + userId));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException();
        }

        // 3. Validate shipping address ownership
        Address shippingAddress = addressRepository
            .findByIdAndUserId(request.shippingAddressId(), userId)
            .orElseThrow(() -> new AddressNotFoundException(
                "Shipping address not found or does not belong to you: "
                    + request.shippingAddressId()));

        // 4. Build order items from cart + deduct stock + compute total
        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (CartItem cartItem : cart.getItems()) {

            Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found: " + cartItem.getProductId()));

            // Stock check (may have changed since add-to-cart)
            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for '%s'. Available: %d, requested: %d",
                        product.getName(), product.getStock(), cartItem.getQuantity()));
            }

            // Use snapshot price from cart (respects price at add-to-cart time)
            double lineTotal = cartItem.getPrice() * cartItem.getQuantity();
            totalPrice += lineTotal;

            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // Snapshot into order item
            OrderItem orderItem = OrderItem.builder()
                .productId(product.getId())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .build();

            orderItems.add(orderItem);
        }

        // 5. Apply Coupon (if provided)
        double discountAmount = 0.0;
        String appliedCouponCode = null;

        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            // validateAndCalculateDiscount throws InvalidCouponException if invalid
            discountAmount = couponService.validateAndCalculateDiscount(request.couponCode(), totalPrice);
            appliedCouponCode = request.couponCode().toUpperCase();
            log.info("Coupon applied during checkout: code={}, discount={}", appliedCouponCode, discountAmount);
        }

        double finalTotalPrice = totalPrice - discountAmount;

        // 6. Build order
        Order order = Order.builder()
            .userId(user.getId())
            .shippingAddressId(shippingAddress.getId())
            .totalPrice(finalTotalPrice)        // <-- Updated with discounted price
            .discountAmount(discountAmount)     // <-- Added
            .couponCode(appliedCouponCode)      // <-- Added
            .status(Status.PLACED)
            .items(orderItems)
            .build();

        orderItems.forEach(item -> item.setOrder(order));

        // 7. Initial status history
        order.getStatusHistory().add(OrderStatusHistory.builder()
            .order(order)
            .fromStatus(null)
            .toStatus(Status.PLACED)
            .changedBy(user.getId())
            .note("Order placed via cart checkout")
            .build());

        // 8. Persist order
        Order saved = orderRepository.save(order);

        // 9. Clear the cart (same transaction — atomic)
        cartService.emptyCartForUser(userId);

        // 10. Record coupon usage (only after successful save)
        if (appliedCouponCode != null) {
            Coupon coupon = couponService.getCouponByCode(appliedCouponCode);
            couponService.recordUsage(coupon);
        }

        log.info("Checkout success: orderId={}, userId={}, items={}, subtotal={}, discount={}, total={}, addressId={}",
            saved.getId(), userId, orderItems.size(), totalPrice, discountAmount, finalTotalPrice, shippingAddress.getId());

        return OrderMapper.toResponse(saved, shippingAddress);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetch shipping address for an order.
     * Returns null if none set (legacy orders) or address deleted.
     */
    private Address fetchShippingAddress(Order order) {
        if (order == null || order.getShippingAddressId() == null) {
            return null;
        }
        return addressRepository.findById(order.getShippingAddressId()).orElse(null);
    }

    /**
     * Batch-fetch shipping addresses for a list of orders — avoids N+1 queries.
     *
     * Strategy:
     *   1. Collect unique shipping address IDs
     *   2. Single query to fetch all of them
     *   3. Build map id → Address
     *   4. Map orders using the cache
     */
    private List<OrderResponse> mapOrdersWithAddresses(List<Order> orders) {

        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect unique non-null address IDs
        Set<Long> addressIds = orders.stream()
            .map(Order::getShippingAddressId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());

        // Single batch query
        Map<Long, Address> addressCache = addressIds.isEmpty()
            ? Collections.emptyMap()
            : addressRepository.findAllById(addressIds).stream()
                .collect(Collectors.toMap(Address::getId, Function.identity()));

        // Map orders using cache
        return orders.stream()
            .map(order -> OrderMapper.toResponse(
                order,
                addressCache.get(order.getShippingAddressId())))   // null-safe
            .toList();
    }

    /**
     * Restore stock for all order items.
     * Used by cancel and status-update-to-CANCELLED flows.
     */
    private void restoreStockForItems(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found: " + item.getProductId()));

            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
    }
}