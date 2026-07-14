package com.yumg.starter.config;

import com.yumg.starter.entities.UserStatus;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/** Rejects access tokens issued before a user security change. */
final class TokenVersionJwtValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error INVALID_TOKEN = new OAuth2Error("invalid_token", "Token is no longer valid", null);
    private final UserRepository users;

    TokenVersionJwtValidator(UserRepository users) {
        this.users = users;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Object version = jwt.getClaim("token_version");
        if (!(version instanceof Number tokenVersion)) return invalid();
        return users.findById(jwt.getSubject())
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(user -> user.getTokenVersion() == tokenVersion.longValue())
                .map(ignored -> OAuth2TokenValidatorResult.success())
                .orElseGet(this::invalid);
    }

    private OAuth2TokenValidatorResult invalid() {
        return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
    }
}
