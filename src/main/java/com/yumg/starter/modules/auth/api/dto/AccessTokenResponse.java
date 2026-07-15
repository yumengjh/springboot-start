package com.yumg.starter.modules.auth.api.dto;

/** Safe authentication response exposed to browser clients. */
public record AccessTokenResponse(String accessToken, String tokenType, long expiresIn) {
    public static AccessTokenResponse from(TokenResponse token) {
        return new AccessTokenResponse(token.accessToken(), token.tokenType(), token.expiresIn());
    }
}
