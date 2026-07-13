package com.yumg.starter.modules.rbac.application;

import com.yumg.starter.entities.User;
import com.yumg.starter.entities.Role;
import com.yumg.starter.entities.Permission;
import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.rbac.api.dto.RoleResponse;
import com.yumg.starter.modules.rbac.infrastructure.PermissionRepository;
import com.yumg.starter.modules.rbac.infrastructure.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RbacService {
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final UserRepository users;
    public RbacService(RoleRepository roles, PermissionRepository permissions, UserRepository users) { this.roles = roles; this.permissions = permissions; this.users = users; }
    @Transactional public void grantDefaultUserRole(User user) { user.grant(roles.findByCode("USER").orElseThrow()); }
    @Transactional(readOnly = true) public java.util.List<RoleResponse> listRoles() { return roles.findAll().stream().map(RoleResponse::from).toList(); }
    @Transactional(readOnly = true) public java.util.List<com.yumg.starter.modules.rbac.api.dto.PermissionResponse> listPermissions() { return permissions.findAll().stream().map(com.yumg.starter.modules.rbac.api.dto.PermissionResponse::from).toList(); }
    @Transactional public void assignRole(String userId, String roleCode) { user(userId).grant(role(roleCode)); }
    @Transactional public void removeRole(String userId, String roleCode) { user(userId).revoke(role(roleCode)); }
    @Transactional public void grantPermission(String roleCode, String permissionCode) { role(roleCode).grant(permission(permissionCode)); }
    @Transactional public void revokePermission(String roleCode, String permissionCode) { role(roleCode).revoke(permission(permissionCode)); }
    private User user(String id) { return users.findById(id).orElseThrow(ApiException::notFound); }
    private Role role(String code) { return roles.findByCode(code).orElseThrow(ApiException::notFound); }
    private Permission permission(String code) { return permissions.findByCode(code).orElseThrow(ApiException::notFound); }
}
