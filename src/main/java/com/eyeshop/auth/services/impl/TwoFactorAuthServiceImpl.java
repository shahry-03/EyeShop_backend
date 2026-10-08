package com.eyeshop.auth.services.impl;

import com.eyeshop.auth.dto.request.TwoFactorDisableRequest;
import com.eyeshop.auth.dto.request.TwoFactorEnableRequest;
import com.eyeshop.auth.dto.request.TwoFactorVerifyRequest;
import com.eyeshop.auth.dto.response.TokenResponse;
import com.eyeshop.auth.dto.response.TwoFactorEnableResponse;
import com.eyeshop.auth.dto.response.TwoFactorSetupResponse;
import com.eyeshop.auth.dto.response.TwoFactorStatusResponse;
import com.eyeshop.auth.entity.RefreshToken;
import com.eyeshop.auth.entity.User;
import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.auth.mapper.UserMapper;
import com.eyeshop.auth.repositories.UserRepository;
import com.eyeshop.auth.security.JwtService;
import com.eyeshop.auth.services.BackupCodeService;
import com.eyeshop.auth.services.TotpService;
import com.eyeshop.auth.services.TwoFactorAuthService;
import com.eyeshop.auth.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    private static final int BACKUP_CODE_COUNT = 10;

    private final UserRepository userRepository;
    private final TotpService totpService;
    private final BackupCodeService backupCodeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.2fa.issuer:Universal Auth}")
    private String issuer;

    // ═══════════════════════════════════════════════════════════
    //  SETUP
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public TwoFactorSetupResponse setup(User user) {
        // Prevent re-setup if already enabled
        if (user.isTwoFactorEnabled()) {
            throw new IllegalStateException(
                "2FA is already enabled. Disable it first.");
        }

        // Generate new secret
        String secret = totpService.generateSecret();
        user.setTotpSecret(secret);
        userRepository.saveAndFlush(user);

        String qrCodeDataUri = totpService.generateQrCodeDataUri(
            secret, user.getEmail(), issuer);
        String otpAuthUrl = totpService.generateOtpAuthUrl(
            secret, user.getEmail(), issuer);

        return new TwoFactorSetupResponse(secret, qrCodeDataUri, otpAuthUrl);
    }

    // ═══════════════════════════════════════════════════════════
    //  ENABLE
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public TwoFactorEnableResponse enable(User user, TwoFactorEnableRequest request) {
        if (user.isTwoFactorEnabled()) {
            throw new IllegalStateException("2FA is already enabled");
        }

        String secret = user.getTotpSecret();
        if (secret == null) {
            throw new IllegalStateException(
                "Call /2fa/setup first to generate secret");
        }

        // Verify TOTP code
        if (!totpService.verifyCode(secret, request.code())) {
            throw new BadCredentialsException("Invalid verification code");
        }

        // Enable 2FA + generate backup codes
        user.setTwoFactorEnabled(true);
        userRepository.saveAndFlush(user);

        List<String> backupCodes = backupCodeService.generateCodes(user, BACKUP_CODE_COUNT);

        log.info("2FA enabled for user: {}", user.getId());

        return new TwoFactorEnableResponse(true, backupCodes);
    }

    // ═══════════════════════════════════════════════════════════
    //  DISABLE
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void disable(User user, TwoFactorDisableRequest request) {
        if (!user.isTwoFactorEnabled()) {
            throw new IllegalStateException("2FA is not enabled");
        }

        // 1. Verify password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        // 2. Verify TOTP code
        if (!totpService.verifyCode(user.getTotpSecret(), request.code())) {
            throw new BadCredentialsException("Invalid verification code");
        }

        // 3. Disable + clean up
        user.setTwoFactorEnabled(false);
        user.setTotpSecret(null);
        userRepository.saveAndFlush(user);

        backupCodeService.invalidateAll(user);

        // 4. Revoke all refresh tokens (force re-login)
        refreshTokenService.revokeAllForUser(user.getId());

        log.info("2FA disabled for user: {}", user.getId());
    }

    // ═══════════════════════════════════════════════════════════
    //  STATUS
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public TwoFactorStatusResponse getStatus(User user) {
        return new TwoFactorStatusResponse(
            user.isTwoFactorEnabled(),
            backupCodeService.countRemaining(user)
        );
    }

    // ═══════════════════════════════════════════════════════════
    //  VERIFY (LOGIN STEP 2)
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public TokenResponse verifyAndCompleteLogin(TwoFactorVerifyRequest request) {
        String tempToken = request.tempToken();

        // 1. Validate temp token
        if (!jwtService.isTwoFactorTempToken(tempToken)) {
            throw new BadCredentialsException("Invalid or expired temporary token");
        }

        UUID userId;
        try {
            userId = jwtService.getUserId(tempToken);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid temporary token");
        }

        // 2. Load user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new BadCredentialsException("2FA is not enabled for this user");
        }

        // 3. Verify code — TOTP or backup code
        String code = request.code().trim();
        boolean verified;

        if (code.matches("^\\d{6}$")) {
            // Looks like a TOTP code
            verified = totpService.verifyCode(user.getTotpSecret(), code);
        } else {
            // Try as backup code
            verified = backupCodeService.verifyAndConsume(user, code);
        }

        if (!verified) {
            throw new BadCredentialsException("Invalid 2FA code");
        }

        // 4. Issue tokens
        RefreshToken refreshToken = refreshTokenService.createForUser(user, null, null);
        String accessToken = jwtService.generateAccessToken(user, refreshToken.getJti()); 
        String refreshTokenValue = jwtService.generateRefreshToken(user, refreshToken.getJti());

        log.info("2FA login complete for user: {}", user.getId());

        return TokenResponse.of(
            accessToken,
            refreshTokenValue,
            jwtService.getJwtExpirationInMillis(),
            UserMapper.toResponse(user)
        );
    }
}