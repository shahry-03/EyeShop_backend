package com.eyeshop.auth.dto.response;

public record TwoFactorStatusResponse(
    boolean enabled,
    long backupCodesRemaining
) {}