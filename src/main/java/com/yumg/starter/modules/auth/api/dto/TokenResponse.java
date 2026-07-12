package com.yumg.starter.modules.auth.api.dto;

public record TokenResponse(String accessToken, String refreshToken, String tokenType,
                            long expiresIn) {
}
