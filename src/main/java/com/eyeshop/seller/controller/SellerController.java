package com.eyeshop.seller.controller;

import com.eyeshop.auth.dto.response.ApiResponse;
import com.eyeshop.auth.entity.User;
import com.eyeshop.seller.dto.request.SellerApplyRequest;
import com.eyeshop.seller.dto.response.SellerResponse;
import com.eyeshop.seller.entity.Seller;
import com.eyeshop.seller.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.eyeshop.seller.dto.response.SellerDashboardStatsResponse;
import com.eyeshop.seller.service.SellerDashboardService;
import com.eyeshop.order.dto.response.SellerOrderResponse;
import com.eyeshop.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final SellerDashboardService sellerDashboardService;

    // Helper to map Entity to DTO
    private SellerResponse mapToDto(Seller s) {
        return new SellerResponse(
            s.getId(), s.getShopName(), s.getShopDescription(), 
            s.isApproved(), s.getCommissionRate(), s.getWalletBalance(), s.getCreatedAt()
        );
    }

    // User applies to become a seller
    @PostMapping("/sellers/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SellerResponse>> apply(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SellerApplyRequest request) {
        
        Seller seller = sellerService.applyForSeller(user.getId(), request.shopName(), request.shopDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Seller application submitted. Awaiting admin approval.", mapToDto(seller)));
    }

    // Seller views their own profile
    @GetMapping("/sellers/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SellerResponse>> getMyProfile(
            @AuthenticationPrincipal User user) {
        Seller seller = sellerService.getSellerByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(mapToDto(seller)));
    }

    // Admin approves the seller
    @PatchMapping("/admin/sellers/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SellerResponse>> approve(@PathVariable Long id) {
        Seller seller = sellerService.approveSeller(id);
        return ResponseEntity.ok(ApiResponse.success("Seller approved successfully", mapToDto(seller)));
    }

    @GetMapping("/sellers/me/wallet")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Double>> getWallet(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
            sellerService.getWalletBalance(user.getId())));
    }

    @GetMapping("/sellers/me/dashboard")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerDashboardStatsResponse>> getDashboardStats(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
            sellerDashboardService.getDashboardStats(user.getId())));
    }

    @GetMapping("/sellers/me/orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<PageResponse<SellerOrderResponse>>> getSellerOrders(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            sellerDashboardService.getSellerOrders(user.getId(), pageable)));
    }
}