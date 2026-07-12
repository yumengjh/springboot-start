package com.yumg.starter.modules.rbac.application;

import com.yumg.starter.entities.User;
import com.yumg.starter.modules.rbac.infrastructure.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RbacService {
    private final RoleRepository roles;
    public RbacService(RoleRepository roles) { this.roles = roles; }
    @Transactional public void grantDefaultUserRole(User user) { user.grant(roles.findByCode("USER").orElseThrow()); }
}
