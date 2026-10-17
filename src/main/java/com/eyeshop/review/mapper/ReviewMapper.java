package com.eyeshop.review.mapper;

import com.eyeshop.review.dto.response.ReviewResponse;
import com.eyeshop.review.entity.Review;

public final class ReviewMapper {
    private ReviewMapper() {}

    public static ReviewResponse toResponse(Review review) {
        if (review == null) return null;
        return new ReviewResponse(
            review.getId(),
            review.getUser().getId(),
            review.getUser().getName(),
            review.getRating(),
            review.getComment(),
            review.isVerifiedPurchase(),
            review.getCreatedAt()
        );
    }
}