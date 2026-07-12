package com.yumg.starter.modules.rbac.api;

import com.yumg.starter.modules.rbac.api.dto.RoleResponse;
import com.yumg.starter.modules.rbac.application.RbacService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rbac")
public class RbacController {
    private final RbacService rbac;
    public RbacController(RbacService rbac) { this.rbac = rbac; }
    @GetMapping("/roles") @PreAuthorize("hasAuthority('system:role:read')")
    public List<RoleResponse> roles() { return rbac.listRoles(); }
    @PutMapping("/users/{userId}/roles/{roleCode}") @PreAuthorize("hasAuthority('system:role:assign')")
    public void assignRole(@PathVariable String userId, @PathVariable String roleCode) { rbac.assignRole(userId, roleCode); }
    @DeleteMapping("/users/{userId}/roles/{roleCode}") @PreAuthorize("hasAuthority('system:role:assign')")
    public void removeRole(@PathVariable String userId, @PathVariable String roleCode) { rbac.removeRole(userId, roleCode); }
    @PutMapping("/roles/{roleCode}/permissions/{permissionCode}") @PreAuthorize("hasAuthority('system:role:write')")
    public void grantPermission(@PathVariable String roleCode, @PathVariable String permissionCode) { rbac.grantPermission(roleCode, permissionCode); }
    @DeleteMapping("/roles/{roleCode}/permissions/{permissionCode}") @PreAuthorize("hasAuthority('system:role:write')")
    public void revokePermission(@PathVariable String roleCode, @PathVariable String permissionCode) { rbac.revokePermission(roleCode, permissionCode); }
}
