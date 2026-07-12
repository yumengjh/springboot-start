package com.yumg.starter.modules.auth.api;

import com.yumg.starter.modules.auth.api.dto.RegisterRequest;
import com.yumg.starter.modules.auth.api.dto.LoginRequest;
import com.yumg.starter.modules.auth.api.dto.TokenResponse;
import com.yumg.starter.modules.auth.api.dto.UserResponse;
import com.yumg.starter.modules.auth.application.AuthenticationUseCase;
import com.yumg.starter.modules.auth.application.RegistrationUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RegistrationUseCase registrationService;
    private final AuthenticationUseCase authenticationService;

    public AuthController(RegistrationUseCase registrationService,
                          AuthenticationUseCase authenticationService) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = registrationService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.id())).body(response);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }
}
