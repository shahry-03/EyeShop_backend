package com.eyeshop.review.service;

import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.review.dto.request.ReviewRequest;
import com.eyeshop.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ReviewService {
    ReviewResponse addReview(Long productId, UUID userId, ReviewRequest request);
    PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable);
    void deleteReview(Long reviewId, UUID userId, boolean isAdmin);
}