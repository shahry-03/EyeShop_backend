package com.eyeshop.auth.controllers;

import com.eyeshop.auth.dto.response.ApiResponse;
import com.eyeshop.auth.dto.response.UserResponse;
import com.eyeshop.auth.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.eyeshop.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // class-level — saare endpoints ADMIN only
public class AdminUserController {

    private final UserService userService;

    /**
     * List all users.
     * Requires 'user:read' permission.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(pageable)));
    }

    /**
     * Get user by ID.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    /**
     * Update user by ID.
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @RequestBody com.eyeshop.auth.dto.request.UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated",
            userService.updateUser(userId, request)));
    }

    /**
     * Enable / disable user.
     */
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponse<Void>> enableUser(
            @PathVariable UUID userId,
            @RequestParam boolean enabled) {
        userService.enableUser(userId, enabled);
        return ResponseEntity.ok(ApiResponse.success(
            enabled ? "User enabled" : "User disabled"));
    }

    /**
     * Delete user.
     * Requires 'user:delete' permission.
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    /**
     * Manually unlock a locked user account (admin action).
     */
    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable UUID userId) {
        userService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User account unlocked"));
    }
}