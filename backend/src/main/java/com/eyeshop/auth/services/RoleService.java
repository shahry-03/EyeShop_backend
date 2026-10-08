package com.eyeshop.auth.services;

import com.eyeshop.auth.dto.response.RoleResponse;
import com.eyeshop.auth.dto.response.UserResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleService {

    // Read
    RoleResponse getRoleById(UUID roleId);
    RoleResponse getRoleByName(String roleName);
    List<RoleResponse> getAllRoles();

    // Create / Update
    RoleResponse createRole(String roleName, String description, Set<String> permissionNames);
    RoleResponse updateRole(UUID roleId, String description, Set<String> permissionNames);

    // Delete
    void deleteRole(UUID roleId);

    // Assignment
    UserResponse assignRoleToUser(UUID userId, UUID roleId);
    UserResponse removeRoleFromUser(UUID userId, UUID roleId);
    Set<RoleResponse> getUserRoles(UUID userId);
}