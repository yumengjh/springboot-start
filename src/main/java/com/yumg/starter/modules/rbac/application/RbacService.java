package com.yumg.starter.modules.rbac.application;

import com.yumg.starter.entities.User;
import com.yumg.starter.entities.Role;
import com.yumg.starter.entities.Permission;
import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.auth.application.TokenService;
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
    private final TokenService tokens;
    public RbacService(RoleRepository roles, PermissionRepository permissions, UserRepository users, TokenService tokens) { this.roles = roles; this.permissions = permissions; this.users = users; this.tokens = tokens; }
    @Transactional public void grantDefaultUserRole(User user) { user.grant(roles.findByCode("USER").orElseThrow()); }
    @Transactional(readOnly = true) public java.util.List<RoleResponse> listRoles() { return roles.findAll().stream().map(RoleResponse::from).toList(); }
    @Transactional(readOnly = true) public java.util.List<com.yumg.starter.modules.rbac.api.dto.PermissionResponse> listPermissions() { return permissions.findAll().stream().map(com.yumg.starter.modules.rbac.api.dto.PermissionResponse::from).toList(); }
    @Transactional public void assignRole(String userId, String roleCode) { User user = user(userId); user.grant(role(roleCode)); invalidate(user); }
    @Transactional public void removeRole(String userId, String roleCode) { User user = user(userId); if ("SUPER_ADMIN".equals(roleCode) && user.isSuperAdmin() && users.countByRoles_Code("SUPER_ADMIN") <= 1) throw ApiException.lastSuperAdminProtected(); user.revoke(role(roleCode)); invalidate(user); }
    @Transactional public void grantPermission(String roleCode, String permissionCode) { Role role = role(roleCode); role.grant(permission(permissionCode)); invalidateRoleMembers(roleCode); }
    @Transactional public void revokePermission(String roleCode, String permissionCode) { Role role = role(roleCode); role.revoke(permission(permissionCode)); invalidateRoleMembers(roleCode); }
    private void invalidateRoleMembers(String roleCode) { users.findDistinctByRoles_Code(roleCode).forEach(this::invalidate); }
    private void invalidate(User user) { user.invalidateSessions(); tokens.revokeAllForUser(user.getId()); }
    private User user(String id) { return users.findById(id).orElseThrow(ApiException::notFound); }
    private Role role(String code) { return roles.findByCode(code).orElseThrow(ApiException::notFound); }
    private Permission permission(String code) { return permissions.findByCode(code).orElseThrow(ApiException::notFound); }
}
