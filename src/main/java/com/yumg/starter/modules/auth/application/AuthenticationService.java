package com.yumg.starter.modules.auth.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.User;
import com.yumg.starter.entities.UserStatus;
import com.yumg.starter.modules.auth.api.dto.LoginRequest;
import com.yumg.starter.modules.auth.api.dto.TokenResponse;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.security.application.BruteForceService;
import com.yumg.starter.modules.security.application.AuditService;
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
    private final AuditService audit;

    public AuthenticationService(UserRepository users, PasswordEncoder passwordEncoder,
                                 TokenService tokens, BruteForceService bruteForce, AuditService audit) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.bruteForce = bruteForce;
        this.audit = audit;
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse login(LoginRequest request) {
        String username = request.username().toLowerCase(Locale.ROOT);
        User user = users.findByUsername(username).orElse(null);
        if (user == null) { bruteForce.recordFailure(username); audit.event("LOGIN_FAILED","User",null,"FAILURE","{\"reason\":\"invalid_credentials\"}"); throw ApiException.invalidCredentials(); }
        user.unlockIfExpired(Instant.now());
        if (user.getStatus() == UserStatus.LOCKED) { audit.event("LOGIN_FAILED","User",user.getId(),"FAILURE","{\"reason\":\"locked\"}"); throw ApiException.accountLocked(); }
        if (user.getStatus() == UserStatus.DISABLED) { audit.event("LOGIN_FAILED","User",user.getId(),"FAILURE","{\"reason\":\"disabled\"}"); throw ApiException.accountDisabled(); }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            if (bruteForce.recordFailure(username) && !user.isSuperAdmin()) { user.lock(bruteForce.lockUntil()); tokens.revokeAllForUser(user.getId()); }
            audit.event("LOGIN_FAILED","User",user.getId(),"FAILURE","{\"reason\":\"invalid_credentials\"}"); throw ApiException.invalidCredentials();
        }
        bruteForce.clear(username);
        TokenResponse response = tokens.issue(user); audit.event("LOGIN_SUCCEEDED","User",user.getId()); return response;
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse refresh(String refreshToken) {
        String tokenHash = TokenService.sha256(refreshToken);
        var session = tokens.findSession(tokenHash).orElseThrow(ApiException::unauthorized);
        User user = users.findById(session.getUserId()).orElseThrow(ApiException::unauthorized);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw ApiException.unauthorized();
        }
        TokenResponse response = tokens.rotate(user, refreshToken); audit.event("TOKEN_REFRESHED","User",user.getId()); return response;
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        tokens.revoke(refreshToken); audit.event("LOGOUT","RefreshSession",null);
    }
}
