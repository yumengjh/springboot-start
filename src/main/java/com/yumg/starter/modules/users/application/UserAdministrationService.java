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
import org.springframework.data.domain.PageRequest;
import com.yumg.starter.common.api.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdministrationService {
    private final UserRepository users; private final TokenService tokens;
    public UserAdministrationService(UserRepository users, TokenService tokens) { this.users = users; this.tokens = tokens; }
    @Transactional(readOnly = true) public List<AdminUserResponse> list() { return users.findAll().stream().map(AdminUserResponse::from).toList(); }
    @Transactional(readOnly = true) public PageResponse<AdminUserResponse> list(String query, UserStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var source = status != null ? users.findByStatus(status, pageable)
                : (query == null || query.isBlank() ? users.findAll(pageable)
                : users.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(query, query, pageable));
        return PageResponse.from(source.map(AdminUserResponse::from));
    }
    @Transactional(readOnly = true) public AdminUserResponse get(String id) { return AdminUserResponse.from(users.findById(id).orElseThrow(ApiException::notFound)); }
    @Transactional public void revokeSessions(String id) { users.findById(id).orElseThrow(ApiException::notFound); tokens.revokeAllForUser(id); }
    @Transactional public AdminUserResponse changeStatus(String id, UserStatus status) {
        User user = users.findById(id).orElseThrow(ApiException::notFound);
        if (status != UserStatus.ACTIVE && user.isSuperAdmin() && users.countByRoles_Code("SUPER_ADMIN") <= 1) {
            throw ApiException.lastSuperAdminProtected();
        }
        switch (status) { case ACTIVE -> { user.enable(); user.unlock(); } case DISABLED -> user.disable(); case LOCKED -> user.lock(Instant.now().plus(15, ChronoUnit.MINUTES)); }
        tokens.revokeAllForUser(id);
        return AdminUserResponse.from(user);
    }
}
