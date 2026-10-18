package com.eyeshop.wishlist.repository;

import com.eyeshop.wishlist.entity.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    Page<WishlistItem> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<WishlistItem> findByUserIdAndProductId(UUID userId, Long productId);

    boolean existsByUserIdAndProductId(UUID userId, Long productId);

    void deleteByUserIdAndProductId(UUID userId, Long productId);
}