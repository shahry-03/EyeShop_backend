package com.eyeshop.auth.services;

import com.eyeshop.auth.dto.request.ChangePasswordRequest;
import com.eyeshop.auth.dto.request.UpdateUserRequest;
import com.eyeshop.auth.dto.response.UserResponse;
import com.eyeshop.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserResponse getUserById(UUID userId);

    UserResponse getUserByEmail(String email);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);       // ← changed

    UserResponse updateUser(UUID userId, UpdateUserRequest request);

    void deleteUser(UUID userId);

    void changePassword(UUID userId, ChangePasswordRequest request);

    void enableUser(UUID userId, boolean enabled);

    void unlockUser(UUID userId);
}