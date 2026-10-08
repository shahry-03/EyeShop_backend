package com.eyeshop.auth.services.impl;

import com.eyeshop.auth.dto.response.RoleResponse;
import com.eyeshop.auth.dto.response.UserResponse;
import com.eyeshop.auth.entity.Permission;
import com.eyeshop.auth.entity.Role;
import com.eyeshop.auth.entity.User;
import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.auth.mapper.RoleMapper;
import com.eyeshop.auth.mapper.UserMapper;
import com.eyeshop.auth.repositories.PermissionRepository;
import com.eyeshop.auth.repositories.RoleRepository;
import com.eyeshop.auth.repositories.UserRepository;
import com.eyeshop.auth.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        return RoleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        return RoleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
            .stream()
            .map(RoleMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public RoleResponse createRole(String roleName, String description, Set<String> permissionNames) {
        if (roleRepository.existsByRoleName(roleName)) {
            throw new IllegalArgumentException("Role already exists: " + roleName);
        }

        Role role = Role.builder()
            .roleName(roleName)
            .description(description)
            .permissions(resolvePermissions(permissionNames))
            .build();

        return RoleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UUID roleId, String description, Set<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        if (description != null) role.setDescription(description);
        if (permissionNames != null) role.setPermissions(resolvePermissions(permissionNames));

        return RoleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        if ("ADMIN".equals(role.getRoleName()) || "USER".equals(role.getRoleName())) {
            throw new IllegalArgumentException("Cannot delete built-in role: " + role.getRoleName());
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public UserResponse assignRoleToUser(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        user.getRoles().add(role);
        User updated = userRepository.save(user);
        return UserMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse removeRoleFromUser(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        user.getRoles().remove(role);
        User updated = userRepository.save(user);
        return UserMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<RoleResponse> getUserRoles(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return RoleMapper.toResponseSet(user.getRoles());
    }

    // ---- Helpers ----

    private Set<Permission> resolvePermissions(Set<String> permissionNames) {
        if (permissionNames == null || permissionNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<Permission> permissions = new HashSet<>();
        for (String name : permissionNames) {
            Permission p = permissionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + name));
            permissions.add(p);
        }
        return permissions;
    }
}