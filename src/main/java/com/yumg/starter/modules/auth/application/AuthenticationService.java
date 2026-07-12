package com.yumg.starter.modules.auth.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.User;
import com.yumg.starter.entities.UserStatus;
import com.yumg.starter.modules.auth.api.dto.LoginRequest;
import com.yumg.starter.modules.auth.api.dto.TokenResponse;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.security.application.BruteForceService;
import java.time.Instant;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService implements AuthenticationUseCase {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final BruteForceService bruteForce;

    public AuthenticationService(UserRepository users, PasswordEncoder passwordEncoder,
                                 TokenService tokens, BruteForceService bruteForce) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.bruteForce = bruteForce;
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse login(LoginRequest request) {
        String username = request.username().toLowerCase(Locale.ROOT);
        User user = users.findByUsername(username).orElse(null);
        if (user == null) { bruteForce.recordFailure(username); throw ApiException.unauthorized(); }
        user.unlockIfExpired(Instant.now());
        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            if (bruteForce.recordFailure(username) && !user.isSuperAdmin()) { user.lock(bruteForce.lockUntil()); tokens.revokeAllForUser(user.getId()); }
            throw ApiException.unauthorized();
        }
        bruteForce.clear(username);
        return tokens.issue(user);
    }

    @Override
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        String tokenHash = TokenService.sha256(refreshToken);
        var session = tokens.findSession(tokenHash).orElseThrow(ApiException::unauthorized);
        User user = users.findById(session.getUserId()).orElseThrow(ApiException::unauthorized);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw ApiException.unauthorized();
        }
        return tokens.rotate(user, refreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        tokens.revoke(refreshToken);
    }
}
