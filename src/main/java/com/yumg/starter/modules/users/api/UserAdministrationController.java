package com.yumg.starter.modules.users.api;

import com.yumg.starter.modules.users.api.dto.AdminUserResponse;
import com.yumg.starter.modules.users.api.dto.UpdateUserStatusRequest;
import com.yumg.starter.modules.users.application.UserAdministrationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdministrationController {
    private final UserAdministrationService users;
    public UserAdministrationController(UserAdministrationService users) { this.users = users; }
    @GetMapping @PreAuthorize("hasAuthority('system:user:read')") public List<AdminUserResponse> list() { return users.list(); }
    @PatchMapping("/{id}/status") @PreAuthorize("hasAuthority('system:user:write')")
    public AdminUserResponse updateStatus(@PathVariable String id, @Valid @RequestBody UpdateUserStatusRequest request) { return users.changeStatus(id, request.status()); }
}
