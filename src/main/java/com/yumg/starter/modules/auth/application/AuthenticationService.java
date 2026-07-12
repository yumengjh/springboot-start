package com.yumg.starter.modules.auth.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.User;
import com.yumg.starter.entities.UserStatus;
import com.yumg.starter.modules.auth.api.dto.LoginRequest;
import com.yumg.starter.modules.auth.api.dto.TokenResponse;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService implements AuthenticationUseCase {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthenticationService(UserRepository users, PasswordEncoder passwordEncoder,
                                 TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = users.findByUsername(request.username().toLowerCase(Locale.ROOT))
                .orElseThrow(ApiException::unauthorized);
        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(request.password(),
                user.getPasswordHash())) {
            throw ApiException.unauthorized();
        }
        return tokens.issue(user);
    }
}
