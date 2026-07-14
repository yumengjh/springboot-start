package com.yumg.starter.modules.auth.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.User;
import com.yumg.starter.modules.auth.api.dto.RegisterRequest;
import com.yumg.starter.modules.auth.api.dto.UserResponse;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.rbac.application.RbacService;
import com.yumg.starter.modules.security.application.AuditService;
import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService implements RegistrationUseCase {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final RbacService rbac;
    private final AuditService audit;
    private final RuntimeSettingService settings;

    public RegistrationService(UserRepository users, PasswordEncoder passwordEncoder, RbacService rbac, AuditService audit, RuntimeSettingService settings) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.rbac = rbac;
        this.audit = audit;
        this.settings = settings;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!settings.enabled("identity.registration.enabled")) throw ApiException.registrationDisabled();
        String username = request.username().toLowerCase(Locale.ROOT);
        if (users.existsByUsername(username)) {
            throw ApiException.conflict();
        }
        User user = new User(username, request.displayName().trim(), passwordEncoder.encode(request.password()));
        rbac.grantDefaultUserRole(user);
        user = users.save(user);
        audit.event("USER_REGISTERED", "User", user.getId());
        return UserResponse.from(user);
    }
}
