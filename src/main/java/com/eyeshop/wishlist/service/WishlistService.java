package com.eyeshop.wishlist.service;

import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.product.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WishlistService {
    void addToWishlist(UUID userId, Long productId);
    void removeFromWishlist(UUID userId, Long productId);
    PageResponse<ProductResponse> getWishlist(UUID userId, Pageable pageable);
}