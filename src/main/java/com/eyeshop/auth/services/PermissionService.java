package com.eyeshop.auth.services;

import com.eyeshop.auth.dto.response.PermissionResponse;

import java.util.List;
import java.util.UUID;

public interface PermissionService {

    PermissionResponse getPermissionById(UUID permissionId);

    List<PermissionResponse> getAllPermissions();

    PermissionResponse createPermission(String name, String description);

    PermissionResponse updatePermission(UUID permissionId, String description);

    void deletePermission(UUID permissionId);
}