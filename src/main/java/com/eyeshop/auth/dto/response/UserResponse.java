package com.eyeshop.auth.dto.response;

import com.eyeshop.auth.entity.Provider;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String name,
    String image,
    boolean enabled,
    Instant createdAt,
    Instant updatedAt,
    Provider provider,
    Set<RoleResponse> roles
) {}