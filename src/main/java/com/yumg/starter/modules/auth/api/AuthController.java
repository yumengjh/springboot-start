package com.yumg.starter.modules.auth.api;

import com.yumg.starter.modules.auth.api.dto.RegisterRequest;
import com.yumg.starter.modules.auth.api.dto.LoginRequest;
import com.yumg.starter.modules.auth.api.dto.AccessTokenResponse;
import com.yumg.starter.modules.auth.api.dto.UserResponse;
import com.yumg.starter.modules.auth.application.AuthenticationUseCase;
import com.yumg.starter.modules.auth.application.RegistrationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证与会话")
public class AuthController {
    static final String REFRESH_COOKIE = "refresh_token";
    private static final long REFRESH_COOKIE_MAX_AGE_SECONDS = 30L * 24 * 60 * 60;
    private final RegistrationUseCase registrationService;
    private final AuthenticationUseCase authenticationService;
    private final boolean secureRefreshCookie;

    public AuthController(RegistrationUseCase registrationService,
                          AuthenticationUseCase authenticationService,
                          @Value("${app.auth.refresh-cookie-secure:false}") boolean secureRefreshCookie) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.secureRefreshCookie = secureRefreshCookie;
    }

    @PostMapping("/register")
    @Operation(summary = "注册账号")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "密码登录")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        var token = authenticationService.login(request);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                refreshCookie(token.refreshToken(), token.persistent()).toString())
                .body(AccessTokenResponse.from(token));
    }

    @PostMapping("/refresh")
    @Operation(summary = "轮换 Refresh Token")
    public ResponseEntity<AccessTokenResponse> refresh(
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken) {
        var token = authenticationService.refresh(refreshToken);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                refreshCookie(token.refreshToken(), token.persistent()).toString())
                .body(AccessTokenResponse.from(token));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出当前设备")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken) {
        authenticationService.logout(refreshToken);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString()).build();
    }

    private ResponseCookie refreshCookie(String value, boolean persistent) {
        ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true).secure(secureRefreshCookie).sameSite("Strict").path("/api/v1/auth");
        if (persistent) cookie.maxAge(REFRESH_COOKIE_MAX_AGE_SECONDS);
        return cookie.build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE, "").httpOnly(true).secure(secureRefreshCookie)
                .sameSite("Strict").path("/api/v1/auth").maxAge(0).build();
    }
}
