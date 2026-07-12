package com.yumg.starter.modules.users.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.User;
import com.yumg.starter.entities.UserStatus;
import com.yumg.starter.modules.auth.application.TokenService;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.users.api.dto.AdminUserResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdministrationService {
    private final UserRepository users; private final TokenService tokens;
    public UserAdministrationService(UserRepository users, TokenService tokens) { this.users = users; this.tokens = tokens; }
    @Transactional(readOnly = true) public List<AdminUserResponse> list() { return users.findAll().stream().map(AdminUserResponse::from).toList(); }
    @Transactional public AdminUserResponse changeStatus(String id, UserStatus status) {
        User user = users.findById(id).orElseThrow(ApiException::notFound);
        switch (status) { case ACTIVE -> { user.enable(); user.unlock(); } case DISABLED -> user.disable(); case LOCKED -> user.lock(Instant.now().plus(15, ChronoUnit.MINUTES)); }
        tokens.revokeAllForUser(id);
        return AdminUserResponse.from(user);
    }
}
