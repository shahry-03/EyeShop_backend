package com.eyeshop.product.dto.response;

public record ProductImageResponse(
    Long id,
    String url,
    String publicId,
    boolean isPrimary,
    int displayOrder
) {}