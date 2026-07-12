package com.yumg.starter.config;

import com.yumg.starter.entities.User;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.rbac.infrastructure.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
public class BootstrapAdminInitializer {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    CommandLineRunner bootstrapAdmin(UserRepository users, RoleRepository roles, PasswordEncoder encoder,
            @Value("${APP_BOOTSTRAP_ADMIN_USERNAME:}") String username,
            @Value("${APP_BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        return args -> {
            if (username.isBlank() || password.isBlank() || roles.findByCode("SUPER_ADMIN").isEmpty()) return;
            if (users.existsByUsername(username.toLowerCase(java.util.Locale.ROOT))) return;
            User admin = new User(username.toLowerCase(java.util.Locale.ROOT), "Super Administrator", encoder.encode(password));
            admin.grant(roles.findByCode("SUPER_ADMIN").orElseThrow());
            users.save(admin);
        };
    }
}
