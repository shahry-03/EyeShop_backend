package com.eyeshop.auth.mapper;

import com.eyeshop.auth.dto.internal.InternalUserResponse;
import com.eyeshop.auth.dto.response.UserResponse;
import com.eyeshop.auth.entity.Permission;
import com.eyeshop.auth.entity.Role;
import com.eyeshop.auth.entity.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Manual mapper for User entity ↔ DTOs.
 */
public final class UserMapper {

    private UserMapper() {
        // utility class
    }

    /**
     * Entity → UserResponse (client-facing).
     * NEVER includes password.
     *
     * @param includeRolePermissions if true, includes full permission tree per role.
     *                               if false, only role names (lighter payload).
     */
    public static UserResponse toResponse(User user, boolean includeRolePermissions) {
        if (user == null) return null;

        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getImage(),
            user.isEnabled(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getProvider(),
            includeRolePermissions
                ? RoleMapper.toResponseSet(user.getRoles())
                : RoleMapper.toSummarySet(user.getRoles())
        );
    }

    /**
     * Convenience — default includes role permissions.
     */
    public static UserResponse toResponse(User user) {
        return toResponse(user, true);
    }

    /**
     * Entity → InternalUserResponse (service-to-service ONLY).
     *
     * ⚠️ Includes hashed password. NEVER expose via public API.
     * Builds flat authority set — BOTH roles (with ROLE_ prefix) and permissions.
     */
    public static InternalUserResponse toInternalResponse(User user) {
        if (user == null) return null;

        return new InternalUserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getPassword(),        // hashed — internal use only
            user.isEnabled(),
            user.getProvider(),
            user.getProviderId(),
            buildAuthorities(user.getRoles())
        );
    }

    /**
     * Builds flat authority set:
     *   - "ROLE_ADMIN", "ROLE_USER"   (roles with ROLE_ prefix)
     *   - "user:read", "user:write"   (permissions as-is)
     */
    private static Set<String> buildAuthorities(Set<Role> roles) {
        Set<String> authorities = new HashSet<>();
        if (roles == null) return authorities;

        for (Role role : roles) {
            if (role.getRoleName() != null) {
                authorities.add("ROLE_" + role.getRoleName());
            }
            if (role.getPermissions() != null) {
                for (Permission permission : role.getPermissions()) {
                    if (permission.getName() != null) {
                        authorities.add(permission.getName());
                    }
                }
            }
        }
        return authorities;
    }
}