package com.yumg.starter.modules.rbac.application;

import com.yumg.starter.entities.Permission;
import com.yumg.starter.entities.Role;
import com.yumg.starter.modules.rbac.infrastructure.PermissionRepository;
import com.yumg.starter.modules.rbac.infrastructure.RoleRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RbacBootstrap implements ApplicationRunner {
    private static final List<String> CODES = List.of("system:user:read", "system:user:write", "system:role:read", "system:role:write", "system:role:assign", "system:config:read", "system:config:write", "system:audit:read", "example:announcement:read", "example:announcement:write", "example:announcement:delete");
    private final PermissionRepository permissions; private final RoleRepository roles;
    public RbacBootstrap(PermissionRepository permissions, RoleRepository roles) { this.permissions = permissions; this.roles = roles; }
    @Override @Transactional public void run(ApplicationArguments args) {
        List<Permission> all = CODES.stream().map(code -> permissions.findByCode(code).orElseGet(() -> permissions.save(new Permission(code, code)))).toList();
        Role superAdmin = roles.findByCode("SUPER_ADMIN").orElseGet(() -> roles.save(new Role("SUPER_ADMIN", "Super Administrator")));
        all.forEach(superAdmin::grant);
        roles.save(superAdmin);
        roles.findByCode("ADMIN").orElseGet(() -> roles.save(new Role("ADMIN", "Administrator")));
        roles.findByCode("USER").orElseGet(() -> roles.save(new Role("USER", "User")));
    }
}
