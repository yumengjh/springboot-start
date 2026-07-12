package com.yumg.starter.modules.auth.application;

import com.yumg.starter.modules.auth.api.dto.RegisterRequest;
import com.yumg.starter.modules.auth.api.dto.UserResponse;

public interface RegistrationUseCase {
    UserResponse register(RegisterRequest request);
}
