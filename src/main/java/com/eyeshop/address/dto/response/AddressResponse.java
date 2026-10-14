package com.eyeshop.address.dto.response;

import com.eyeshop.address.entity.AddressLabel;

import java.time.LocalDateTime;

public record AddressResponse(
    Long id,
    AddressLabel label,
    String fullName,
    String phone,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String country,
    boolean isDefault,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}