package com.yumg.starter.modules.auth.application;

import com.yumg.starter.modules.auth.api.dto.LoginRequest;
import com.yumg.starter.modules.auth.api.dto.TokenResponse;

public interface AuthenticationUseCase {
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(String refreshToken);
    void logout(String refreshToken);
}
