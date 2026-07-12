package com.yumg.starter.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.yumg.starter.entities.Role;
import com.yumg.starter.entities.User;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.rbac.infrastructure.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite:target/bootstrap-admin-service-test.db",
        "APP_BOOTSTRAP_ADMIN_USERNAME=",
        "APP_BOOTSTRAP_ADMIN_PASSWORD="
})
class BootstrapAdminServiceTest {

    @Autowired
    private BootstrapAdminService bootstrapAdminService;

    @Autowired
    private UserRepository users;

    @Autowired
    private RoleRepository roles;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    @Transactional
    void resetsExistingAdministratorAndEnsuresSuperAdminRole() {
        Role superAdmin = roles.findByCode("SUPER_ADMIN").orElseThrow();
        User user = new User("recovery-admin", "Recovery Admin", encoder.encode("old-password"));
        users.saveAndFlush(user);

        bootstrapAdminService.initialize("recovery-admin", "new-password", true);

        User updated = users.findByUsername("recovery-admin").orElseThrow();
        assertThat(encoder.matches("new-password", updated.getPasswordHash())).isTrue();
        assertThat(updated.isSuperAdmin()).isTrue();
    }
}
