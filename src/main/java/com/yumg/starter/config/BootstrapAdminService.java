package com.yumg.starter.config;

import com.yumg.starter.entities.Role;
import com.yumg.starter.entities.User;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.rbac.infrastructure.RoleRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BootstrapAdminService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public BootstrapAdminService(UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional
    public void initialize(String username, String password, boolean resetPassword) {
        if (username.isBlank() || password.isBlank()) return;

        Role superAdmin = roles.findByCode("SUPER_ADMIN").orElse(null);
        if (superAdmin == null) return;

        String normalized = username.toLowerCase(Locale.ROOT);
        User admin = users.findByUsername(normalized).orElse(null);
        if (admin == null) {
            admin = new User(normalized, "Super Administrator", encoder.encode(password));
            admin.grant(superAdmin);
            users.save(admin);
            return;
        }

        if (!resetPassword) return;
        admin.changePasswordHash(encoder.encode(password));
        admin.enable();
        admin.unlock();
        if (!admin.isSuperAdmin()) admin.grant(superAdmin);
    }
}
