package com.eyeshop.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID; 

public record ReviewResponse(
    Long id,
    UUID userId,       
    String userName,
    Integer rating,
    String comment,
    boolean isVerifiedPurchase,
    LocalDateTime createdAt
) {}