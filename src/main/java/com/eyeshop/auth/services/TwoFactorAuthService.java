package com.eyeshop.auth.services;

import com.eyeshop.auth.dto.request.TwoFactorDisableRequest;
import com.eyeshop.auth.dto.request.TwoFactorEnableRequest;
import com.eyeshop.auth.dto.request.TwoFactorVerifyRequest;
import com.eyeshop.auth.dto.response.TokenResponse;
import com.eyeshop.auth.dto.response.TwoFactorEnableResponse;
import com.eyeshop.auth.dto.response.TwoFactorSetupResponse;
import com.eyeshop.auth.dto.response.TwoFactorStatusResponse;
import com.eyeshop.auth.entity.User;

public interface TwoFactorAuthService {

    /**
     * Generate new TOTP secret + QR code for the user.
     * Does NOT enable 2FA yet — user must confirm with /enable.
     */
    TwoFactorSetupResponse setup(User user);

    /**
     * Verify TOTP code and enable 2FA.
     * Generates backup codes.
     */
    TwoFactorEnableResponse enable(User user, TwoFactorEnableRequest request);

    /**
     * Disable 2FA (requires password + current TOTP code).
     */
    void disable(User user, TwoFactorDisableRequest request);

    /**
     * Get current 2FA status.
     */
    TwoFactorStatusResponse getStatus(User user);

    /**
     * Step 2 of login: verify TOTP or backup code, return full tokens.
     */
    TokenResponse verifyAndCompleteLogin(TwoFactorVerifyRequest request);
}