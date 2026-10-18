package com.eyeshop.wishlist.service.impl;

import com.eyeshop.auth.entity.User;
import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.auth.repositories.UserRepository;
import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.product.dto.response.ProductResponse;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.exception.ProductNotFoundException;
import com.eyeshop.product.mapper.ProductMapper;
import com.eyeshop.product.repository.ProductRepository;
import com.eyeshop.wishlist.entity.WishlistItem;
import com.eyeshop.wishlist.repository.WishlistRepository;
import com.eyeshop.wishlist.service.WishlistService;
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
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void addToWishlist(UUID userId, Long productId) {
        // Idempotent: Agar pehle se hai, toh dobara add mat karo
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            log.info("Product {} already in wishlist for user {}", productId, userId);
            return;
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        WishlistItem item = WishlistItem.builder()
            .user(user)
            .product(product)
            .build();

        wishlistRepository.save(item);
        log.info("Added to wishlist: userId={}, productId={}", userId, productId);
    }

    @Override
    @Transactional
    public void removeFromWishlist(UUID userId, Long productId) {
        wishlistRepository.findByUserIdAndProductId(userId, productId)
            .ifPresent(item -> {
                wishlistRepository.delete(item);
                log.info("Removed from wishlist: userId={}, productId={}", userId, productId);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getWishlist(UUID userId, Pageable pageable) {
        Page<WishlistItem> page = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // WishlistItem se Product nikalo aur DTO mein map karo
        List<ProductResponse> productResponses = page.getContent().stream()
            .map(item -> ProductMapper.toResponse(item.getProduct()))
            .toList();

        return PageResponse.of(productResponses, page);
    }
}