package com.eyeshop.auth.dto.response;

import java.util.Set;
import java.util.UUID;

public record RoleResponse(
    UUID id,
    String roleName,              // "ADMIN", "USER" — without ROLE_ prefix
    String description,
    Set<PermissionResponse> permissions
) {}