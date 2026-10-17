package com.eyeshop.review.service.impl;

import com.eyeshop.auth.entity.User;
import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.auth.repositories.UserRepository;
import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.order.repository.OrderRepository;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.exception.ProductNotFoundException;
import com.eyeshop.product.repository.ProductRepository;
import com.eyeshop.review.dto.request.ReviewRequest;
import com.eyeshop.review.dto.response.ReviewResponse;
import com.eyeshop.review.entity.Review;
import com.eyeshop.review.exception.AlreadyReviewedException;
import com.eyeshop.review.mapper.ReviewMapper;
import com.eyeshop.review.repository.ReviewRepository;
import com.eyeshop.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ReviewResponse addReview(Long productId, UUID userId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
            
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Check if user already reviewed
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new AlreadyReviewedException("You have already reviewed this product.");
        }

        // 2. Check Verified Purchase
        boolean isVerified = orderRepository.hasUserPurchasedProduct(userId, productId);

        Review review = Review.builder()
            .product(product)
            .user(user)
            .rating(request.rating())
            .comment(request.comment())
            .isVerifiedPurchase(isVerified)
            .build();

        Review saved = reviewRepository.saveAndFlush(review);

        // 3. Update Product Average Rating
        updateProductRating(product);

        log.info("Review added: id={}, productId={}, rating={}, verified={}", saved.getId(), productId, request.rating(), isVerified);
        return ReviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found: " + productId);
        }
        Page<Review> page = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        return PageResponse.from(page, ReviewMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, UUID userId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own reviews.");
        }

        Product product = review.getProduct();
        reviewRepository.delete(review);
        reviewRepository.flush();

        updateProductRating(product);
        log.info("Review deleted: id={}, byAdmin={}", reviewId, isAdmin);
    }

    // Helper to recalculate and save average rating
    private void updateProductRating(Product product) {
        Double avg = reviewRepository.getAverageRatingByProductId(product.getId());
        long count = reviewRepository.countByProductId(product.getId());

        product.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0); // Round to 1 decimal
        product.setTotalReviews((int) count);
        productRepository.saveAndFlush(product);
    }
}