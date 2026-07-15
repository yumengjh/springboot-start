package com.yumg.starter.modules.rbac.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yumg.starter.entities.Permission;
import com.yumg.starter.entities.Role;
import com.yumg.starter.modules.rbac.infrastructure.PermissionRepository;
import com.yumg.starter.modules.rbac.infrastructure.RoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.DefaultApplicationArguments;

class RbacBootstrapTest {
    @Test
    void replacesLegacyBuiltInPermissionCodePlaceholderWithReadableDescription() throws Exception {
        PermissionRepository permissions = Mockito.mock(PermissionRepository.class);
        RoleRepository roles = Mockito.mock(RoleRepository.class);
        Permission legacy = new Permission("system:user:read", "system:user:read");
        when(permissions.findByCode("system:user:read")).thenReturn(Optional.of(legacy));
        when(permissions.findByCode(Mockito.argThat(code -> !"system:user:read".equals(code))))
                .thenReturn(Optional.empty());
        when(permissions.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roles.findByCode("SUPER_ADMIN")).thenReturn(Optional.of(new Role("SUPER_ADMIN", "超级管理员")));
        when(roles.findByCode("ADMIN")).thenReturn(Optional.of(new Role("ADMIN", "管理员")));
        when(roles.findByCode("USER")).thenReturn(Optional.of(new Role("USER", "普通用户")));

        new RbacBootstrap(permissions, roles).run(new DefaultApplicationArguments());

        assertThat(legacy.getDescription()).isEqualTo("查看用户列表与用户资料");
    }
}
