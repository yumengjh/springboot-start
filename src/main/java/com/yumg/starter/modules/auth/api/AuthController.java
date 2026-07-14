package com.yumg.starter.modules.auth.api;

import com.yumg.starter.modules.auth.api.dto.RegisterRequest;
import com.yumg.starter.modules.auth.api.dto.LoginRequest;
import com.yumg.starter.modules.auth.api.dto.TokenResponse;
import com.yumg.starter.modules.auth.api.dto.RefreshRequest;
import com.yumg.starter.modules.auth.api.dto.UserResponse;
import com.yumg.starter.modules.auth.application.AuthenticationUseCase;
import com.yumg.starter.modules.auth.application.RegistrationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证与会话")
public class AuthController {
    private final RegistrationUseCase registrationService;
    private final AuthenticationUseCase authenticationService;

    public AuthController(RegistrationUseCase registrationService,
                          AuthenticationUseCase authenticationService) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    @Operation(summary = "注册账号")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "密码登录")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "轮换 Refresh Token")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authenticationService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "登出当前设备")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
