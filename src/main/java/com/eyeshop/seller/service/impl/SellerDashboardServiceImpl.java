package com.eyeshop.seller.service.impl;

import com.eyeshop.address.entity.Address;
import com.eyeshop.address.mapper.AddressMapper;
import com.eyeshop.address.repository.AddressRepository;
import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.order.dto.response.OrderItemResponse;
import com.eyeshop.order.dto.response.SellerOrderResponse;
import com.eyeshop.order.entity.Order;
import com.eyeshop.order.mapper.OrderMapper;
import com.eyeshop.order.repository.OrderRepository;
import com.eyeshop.seller.dto.response.SellerDashboardStatsResponse;
import com.eyeshop.seller.entity.Seller;
import com.eyeshop.seller.repository.SellerRepository;
import com.eyeshop.seller.service.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;

    private Seller getSellerOrThrow(UUID userId) {
        return sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Seller profile not found. Please apply to become a seller."));
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDashboardStatsResponse getDashboardStats(UUID userId) {
        Seller seller = getSellerOrThrow(userId);
        Long sellerId = seller.getId();

        Double totalSales = orderRepository.getTotalSalesBySellerId(sellerId);
        Long totalOrders = orderRepository.getTotalOrdersBySellerId(sellerId);
        Long pendingOrders = orderRepository.getPendingOrdersBySellerId(sellerId);

        return new SellerDashboardStatsResponse(
            totalSales,
            totalOrders,
            pendingOrders,
            seller.getWalletBalance()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerOrderResponse> getSellerOrders(UUID userId, Pageable pageable) {
        Seller seller = getSellerOrThrow(userId);
        Long sellerId = seller.getId();

        Page<Order> page = orderRepository.findOrdersBySellerId(sellerId, pageable);

        List<SellerOrderResponse> content = page.getContent().stream().map(order -> {
            // Filter items to only show this seller's items
            List<OrderItemResponse> myItems = order.getItems().stream()
                .filter(item -> sellerId.equals(item.getSellerId()))
                .map(OrderMapper::toItemResponse)
                .toList();

            // Calculate total earning for this seller for this specific order
            double myTotalEarning = myItems.stream()
                .mapToDouble(OrderItemResponse::sellerEarning)
                .sum();

            // Fetch shipping address
            Address address = order.getShippingAddressId() != null 
                ? addressRepository.findById(order.getShippingAddressId()).orElse(null) 
                : null;

            return new SellerOrderResponse(
                order.getId(),
                order.getUserId(),
                myTotalEarning,
                order.getStatus(),
                order.getCreatedAt(),
                AddressMapper.toResponse(address),
                myItems
            );
        }).toList();

        return PageResponse.of(content, page);
    }
}