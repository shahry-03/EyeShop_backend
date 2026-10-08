package com.eyeshop.auth.services;

import com.eyeshop.auth.entity.User;

public interface AccountLockoutService {

    /**
     * Check if account is locked. Throws AccountLockedException if locked.
     */
    void checkAccountLocked(User user);

    /**
     * Record a failed login attempt.
     * If threshold exceeded, locks the account.
     * Returns true if account was just locked.
     */
    boolean recordFailedAttempt(User user);

    /**
     * Reset failed attempts counter (on successful login).
     */
    void resetFailedAttempts(User user);

    /**
     * Manually unlock an account (admin action).
     */
    void unlockAccount(User user);
}