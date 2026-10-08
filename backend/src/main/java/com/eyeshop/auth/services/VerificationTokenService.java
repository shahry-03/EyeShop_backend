package com.eyeshop.auth.services;

import com.eyeshop.auth.entity.User;
import com.eyeshop.auth.entity.VerificationToken;

public interface VerificationTokenService {

    VerificationToken createEmailVerificationToken(User user);

    VerificationToken createPasswordResetToken(User user);

    VerificationToken validateToken(String token, VerificationToken.TokenType type);

    void markUsed(VerificationToken token);
}