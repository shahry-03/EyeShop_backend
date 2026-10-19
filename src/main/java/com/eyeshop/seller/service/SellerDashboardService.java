package com.eyeshop.seller.service;

import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.order.dto.response.SellerOrderResponse;
import com.eyeshop.seller.dto.response.SellerDashboardStatsResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SellerDashboardService {
    SellerDashboardStatsResponse getDashboardStats(UUID userId);
    PageResponse<SellerOrderResponse> getSellerOrders(UUID userId, Pageable pageable);
}