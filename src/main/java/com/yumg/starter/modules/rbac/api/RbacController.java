package com.yumg.starter.modules.rbac.api;

import com.yumg.starter.modules.rbac.api.dto.RoleResponse;
import com.yumg.starter.modules.rbac.api.dto.PermissionResponse;
import com.yumg.starter.modules.rbac.application.RbacService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.yumg.starter.modules.rbac.api.dto.RoleRequest;
import com.yumg.starter.modules.rbac.api.dto.PermissionRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/rbac")
@Tag(name = "RBAC") @SecurityRequirement(name = "bearerAuth")
@Validated
public class RbacController {
    private final RbacService rbac;
    public RbacController(RbacService rbac) { this.rbac = rbac; }
    @GetMapping("/roles") @PreAuthorize("hasAuthority('system:role:read')")
    public List<RoleResponse> roles() { return rbac.listRoles(); }
    @GetMapping("/permissions") @PreAuthorize("hasAuthority('system:role:read')")
    public List<PermissionResponse> permissions() { return rbac.listPermissions(); }
    @PostMapping("/roles") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('system:role:write')") public RoleResponse createRole(@Valid @RequestBody RoleRequest request){return rbac.createRole(request);}
    @PutMapping("/roles/{roleCode}") @PreAuthorize("hasAuthority('system:role:write')") public RoleResponse updateRole(@PathVariable @Pattern(regexp = "[A-Z_]{2,64}") String roleCode,@Valid @RequestBody RoleRequest request){return rbac.updateRole(roleCode,request);}
    @DeleteMapping("/roles/{roleCode}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('system:role:write')") public void deleteRole(@PathVariable @Pattern(regexp = "[A-Z_]{2,64}") String roleCode){rbac.deleteRole(roleCode);}
    @PostMapping("/permissions") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('system:role:write')") public PermissionResponse createPermission(@Valid @RequestBody PermissionRequest request){return rbac.createPermission(request);}
    @PutMapping("/permissions/{permissionCode}") @PreAuthorize("hasAuthority('system:role:write')") public PermissionResponse updatePermission(@PathVariable @Pattern(regexp = "[a-z]+:[a-z-]+:[a-z]+") String permissionCode,@Valid @RequestBody PermissionRequest request){return rbac.updatePermission(permissionCode,request);}
    @DeleteMapping("/permissions/{permissionCode}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('system:role:write')") public void deletePermission(@PathVariable @Pattern(regexp = "[a-z]+:[a-z-]+:[a-z]+") String permissionCode){rbac.deletePermission(permissionCode);}
    @PutMapping("/users/{userId}/roles/{roleCode}") @PreAuthorize("hasAuthority('system:role:assign')")
    public void assignRole(@PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}") String userId, @PathVariable @Pattern(regexp = "[A-Z_]{2,64}") String roleCode) { rbac.assignRole(userId, roleCode); }
    @DeleteMapping("/users/{userId}/roles/{roleCode}") @PreAuthorize("hasAuthority('system:role:assign')")
    public void removeRole(@PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}") String userId, @PathVariable @Pattern(regexp = "[A-Z_]{2,64}") String roleCode) { rbac.removeRole(userId, roleCode); }
    @PutMapping("/roles/{roleCode}/permissions/{permissionCode}") @PreAuthorize("hasAuthority('system:role:write')")
    public void grantPermission(@PathVariable @Pattern(regexp = "[A-Z_]{2,64}") String roleCode, @PathVariable @Pattern(regexp = "[a-z]+:[a-z-]+:[a-z]+") String permissionCode) { rbac.grantPermission(roleCode, permissionCode); }
    @DeleteMapping("/roles/{roleCode}/permissions/{permissionCode}") @PreAuthorize("hasAuthority('system:role:write')")
    public void revokePermission(@PathVariable @Pattern(regexp = "[A-Z_]{2,64}") String roleCode, @PathVariable @Pattern(regexp = "[a-z]+:[a-z-]+:[a-z]+") String permissionCode) { rbac.revokePermission(roleCode, permissionCode); }
}
