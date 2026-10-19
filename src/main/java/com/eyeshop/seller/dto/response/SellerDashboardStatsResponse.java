package com.eyeshop.seller.dto.response;

public record SellerDashboardStatsResponse(
    Double totalSales,
    Long totalOrders,
    Long pendingOrders,
    Double walletBalance
) {}