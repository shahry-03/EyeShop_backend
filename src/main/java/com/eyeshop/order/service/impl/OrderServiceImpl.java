package com.eyeshop.order.service.impl;

import com.eyeshop.address.entity.Address;
import com.eyeshop.address.exception.AddressNotFoundException;
import com.eyeshop.address.repository.AddressRepository;
import com.eyeshop.auth.entity.User;
import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.auth.repositories.UserRepository;
import com.eyeshop.cart.entity.Cart;
import com.eyeshop.cart.entity.CartItem;
import com.eyeshop.cart.exception.CartNotFoundException;
import com.eyeshop.cart.repository.CartRepository;
import com.eyeshop.cart.service.CartService;
import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.coupon.entity.Coupon;
import com.eyeshop.coupon.service.CouponService;
import com.eyeshop.notification.service.NotificationService;
import com.eyeshop.order.dto.request.CheckoutRequest;
import com.eyeshop.order.dto.request.OrderItemRequest;
import com.eyeshop.order.dto.request.OrderRequest;
import com.eyeshop.order.dto.request.OrderStatusUpdateRequest;
import com.eyeshop.order.dto.response.OrderResponse;
import com.eyeshop.order.entity.Order;
import com.eyeshop.order.entity.OrderItem;
import com.eyeshop.order.entity.OrderStatusHistory;
import com.eyeshop.order.entity.Status;
import com.eyeshop.order.exception.EmptyCartException;
import com.eyeshop.order.exception.InsufficientStockException;
import com.eyeshop.order.exception.InvalidStatusTransitionException;
import com.eyeshop.order.mapper.OrderMapper;
import com.eyeshop.order.repository.OrderRepository;
import com.eyeshop.order.service.OrderService;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.repository.ProductRepository;
import com.eyeshop.seller.entity.Seller;
import com.eyeshop.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final NotificationService notificationService;
    
    // Injecting SellerRepository for Wallet Credit
    private final SellerRepository sellerRepository;

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
    //  PLACE ORDER (Direct Order - Marketplace Aware)
    // ═══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public OrderResponse placeOrder(UUID userId, OrderRequest request) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Address shippingAddress = addressRepository
            .findByIdAndUserId(request.shippingAddressId(), userId)
            .orElseThrow(() -> new AddressNotFoundException("Shipping address not found: " + request.shippingAddressId()));

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (OrderItemRequest itemReq : request.itemsList()) {
            Product product = productRepository.findById(itemReq.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

            if (product.getStock() < itemReq.quantity()) {
                throw new InsufficientStockException("Insufficient stock for " + product.getName());
            }

            double lineTotal = product.getPrice() * itemReq.quantity();
            totalPrice += lineTotal;

            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            // ═══ Commission Calculation ═══
            Long sellerId = null;
            Double commissionRate = 0.0;
            Double sellerEarning = lineTotal;
            Double platformEarning = 0.0;

            if (product.getSeller() != null) {
                sellerId = product.getSeller().getId();
                commissionRate = product.getSeller().getCommissionRate();
                platformEarning = lineTotal * (commissionRate / 100.0);
                sellerEarning = lineTotal - platformEarning;
            }

            OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .quantity(itemReq.quantity())
                .price(product.getPrice())
                .sellerId(sellerId)
                .commissionRate(commissionRate)
                .sellerEarning(sellerEarning)
                .platformEarning(platformEarning)
                .build();

            orderItems.add(item);
        }

        Order order = Order.builder()
            .userId(user.getId())
            .shippingAddressId(shippingAddress.getId())
            .totalPrice(totalPrice)
            .status(Status.PLACED)
            .items(orderItems)
            .build();

        orderItems.forEach(item -> item.setOrder(order));

        order.getStatusHistory().add(OrderStatusHistory.builder()
            .order(order)
            .fromStatus(null)
            .toStatus(Status.PLACED)
            .changedBy(user.getId())
            .note("Order placed directly")
            .build());

        Order saved = orderRepository.save(order);
        log.info("Order placed: id={}, total={}", saved.getId(), totalPrice);

        return OrderMapper.toResponse(saved, shippingAddress);
    }

    // ═══════════════════════════════════════════════════════════════
    //  CHECKOUT (Cart to Order - Marketplace Aware)
    // ═══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public OrderResponse checkout(UUID userId, CheckoutRequest request) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException();
        }

        Address shippingAddress = addressRepository
            .findByIdAndUserId(request.shippingAddressId(), userId)
            .orElseThrow(() -> new AddressNotFoundException("Shipping address not found: " + request.shippingAddressId()));

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (CartItem cartItem : cart.getItems()) {

            Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for " + product.getName());
            }

            double lineTotal = cartItem.getPrice() * cartItem.getQuantity();
            totalPrice += lineTotal;

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // ═══ Commission Calculation ═══
            Long sellerId = null;
            Double commissionRate = 0.0;
            Double sellerEarning = lineTotal;
            Double platformEarning = 0.0;

            if (product.getSeller() != null) {
                sellerId = product.getSeller().getId();
                commissionRate = product.getSeller().getCommissionRate();
                platformEarning = lineTotal * (commissionRate / 100.0);
                sellerEarning = lineTotal - platformEarning;
            }

            OrderItem orderItem = OrderItem.builder()
                .productId(product.getId())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .sellerId(sellerId)
                .commissionRate(commissionRate)
                .sellerEarning(sellerEarning)
                .platformEarning(platformEarning)
                .build();

            orderItems.add(orderItem);
        }

        // Coupon Logic
        double discountAmount = 0.0;
        String appliedCouponCode = null;

        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            discountAmount = couponService.validateAndCalculateDiscount(request.couponCode(), totalPrice);
            appliedCouponCode = request.couponCode().toUpperCase();
        }

        double finalTotalPrice = totalPrice - discountAmount;

        Order order = Order.builder()
            .userId(user.getId())
            .shippingAddressId(shippingAddress.getId())
            .totalPrice(finalTotalPrice)
            .discountAmount(discountAmount)
            .couponCode(appliedCouponCode)
            .status(Status.PLACED)
            .items(orderItems)
            .build();

        orderItems.forEach(item -> item.setOrder(order));

        order.getStatusHistory().add(OrderStatusHistory.builder()
            .order(order)
            .fromStatus(null)
            .toStatus(Status.PLACED)
            .changedBy(user.getId())
            .note("Order placed via cart checkout")
            .build());

        Order saved = orderRepository.save(order);

        cartService.emptyCartForUser(userId);

        if (appliedCouponCode != null) {
            Coupon coupon = couponService.getCouponByCode(appliedCouponCode);
            couponService.recordUsage(coupon);
        }

        notificationService.sendNotification(
           userId, "Order Placed Successfully",
           "Your order #" + saved.getId() + " has been placed successfully.", "ORDER_PLACED"
        );

        log.info("Checkout success: orderId={}, total={}", saved.getId(), finalTotalPrice);

        return OrderMapper.toResponse(saved, shippingAddress);
    }

    // ═══════════════════════════════════════════════════════════════
    //  UPDATE STATUS (Marketplace Payout Logic Added)
    // ═══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request, UUID adminUserId) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        Status current = order.getStatus();
        Status target = request.status();

        if (current == target) {
            throw new InvalidStatusTransitionException("Order is already in status: " + current);
        }

        Set<Status> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidStatusTransitionException(
                String.format("Invalid transition: %s → %s. Allowed: %s", current, target, allowed));
        }

        if (target == Status.CANCELLED) {
            restoreStockForItems(order);
        }

        order.setStatus(target);

        order.getStatusHistory().add(OrderStatusHistory.builder()
            .order(order)
            .fromStatus(current)
            .toStatus(target)
            .changedBy(adminUserId)
            .note(request.note() != null && !request.note().isBlank() ? request.note() : "Status changed by admin")
            .build());

        Order saved = orderRepository.save(order);

        // ═══ NEW: Credit Seller Wallets on DELIVERED ═══
        if (target == Status.DELIVERED && current != Status.DELIVERED) {
            for (OrderItem item : order.getItems()) {
                if (item.getSellerId() != null && item.getSellerEarning() != null && item.getSellerEarning() > 0) {
                    Optional<Seller> sellerOpt = sellerRepository.findById(item.getSellerId());
                    if (sellerOpt.isPresent()) {
                        Seller seller = sellerOpt.get();
                        seller.setWalletBalance(seller.getWalletBalance() + item.getSellerEarning());
                        sellerRepository.save(seller);
                        log.info("Payout: Credited Rs.{} to Seller ID {} for OrderItem ID {}", 
                                 item.getSellerEarning(), seller.getId(), item.getId());
                    }
                }
            }
        }

        // Notification Logic
        String title = "Order Status Updated";
        String message = "Your order #" + orderId + " status is now " + target.name();

        if (target == Status.SHIPPED) {
            title = "Order Shipped!";
            message = "Your order #" + orderId + " has been shipped.";
        } else if (target == Status.DELIVERED) {
            title = "Order Delivered!";
            message = "Your order #" + orderId + " has been delivered. Thank you for shopping!";
        }

        notificationService.sendNotification(order.getUserId(), title, message, "ORDER_" + target.name());

        log.info("Order status changed: id={}, {} → {}", orderId, current, target);

        return OrderMapper.toResponse(saved, fetchShippingAddress(saved));
    }

    // ═══════════════════════════════════════════════════════════════
    //  READ METHODS (Unchanged, just keeping them here)
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, UUID requestingUserId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!isAdmin && !order.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("You are not allowed to view this order");
        }

        return OrderMapper.toResponse(order, fetchShippingAddress(order));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByUserId(UUID userId, Pageable pageable) {
        Page<Order> page = orderRepository.findByUserId(userId, pageable);
        return PageResponse.of(mapOrdersWithAddresses(page.getContent()), page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> page = orderRepository.findAll(pageable);
        return PageResponse.of(mapOrdersWithAddresses(page.getContent()), page);
    }

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

        if (!order.getStatus().isCancellable()) {
            throw new InvalidStatusTransitionException("Cannot cancel order in status " + order.getStatus());
        }

        restoreStockForItems(order);

        Status oldStatus = order.getStatus();
        order.setStatus(Status.CANCELLED);

        order.getStatusHistory().add(OrderStatusHistory.builder()
            .order(order)
            .fromStatus(oldStatus)
            .toStatus(Status.CANCELLED)
            .changedBy(requestingUserId)
            .note(isAdmin ? "Cancelled by admin" : "Cancelled by customer")
            .build());

        Order saved = orderRepository.save(order);
        log.info("Order cancelled: id={}, by={}", orderId, requestingUserId);

        return OrderMapper.toResponse(saved, fetchShippingAddress(saved));
    }

    // ═══════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS (Unchanged)
    // ═══════════════════════════════════════════════════════════════

    private Address fetchShippingAddress(Order order) {
        if (order == null || order.getShippingAddressId() == null) return null;
        return addressRepository.findById(order.getShippingAddressId()).orElse(null);
    }

    private List<OrderResponse> mapOrdersWithAddresses(List<Order> orders) {
        if (orders == null || orders.isEmpty()) return Collections.emptyList();

        Set<Long> addressIds = orders.stream()
            .map(Order::getShippingAddressId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Long, Address> addressCache = addressIds.isEmpty()
            ? Collections.emptyMap()
            : addressRepository.findAllById(addressIds).stream()
                .collect(Collectors.toMap(Address::getId, Function.identity()));

        return orders.stream()
            .map(order -> OrderMapper.toResponse(order, addressCache.get(order.getShippingAddressId())))
            .toList();
    }

    private void restoreStockForItems(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
    }
}