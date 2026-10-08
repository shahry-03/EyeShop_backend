package com.eyeshop.auth.services;

import com.eyeshop.auth.dto.request.LoginRequest;
import com.eyeshop.auth.dto.request.RegisterRequest;
import com.eyeshop.auth.dto.response.TokenResponse;
import com.eyeshop.auth.dto.response.UserResponse;
import com.eyeshop.auth.dto.request.ForgotPasswordRequest;
import com.eyeshop.auth.dto.request.ResetPasswordRequest;

public interface AuthService {

    UserResponse registerUser(RegisterRequest request);

    TokenResponse login(LoginRequest request, String ipAddress, String userAgent);

    TokenResponse refresh(String refreshToken);

    void logout(String refreshToken);

    //Email Verification
    void verifyEmail(String token);
    void resendVerificationEmail(String email);

    // ─── Password Reset ────────────────────────
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}