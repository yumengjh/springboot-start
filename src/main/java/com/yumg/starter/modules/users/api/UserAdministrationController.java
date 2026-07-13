package com.yumg.starter.modules.users.api;

import com.yumg.starter.modules.users.api.dto.AdminUserResponse;
import com.yumg.starter.modules.users.api.dto.UpdateUserStatusRequest;
import com.yumg.starter.modules.users.application.UserAdministrationService;
import com.yumg.starter.entities.UserStatus;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.yumg.starter.common.api.PageResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/v1/admin/users")
@Validated
public class UserAdministrationController {
    private final UserAdministrationService users;
    public UserAdministrationController(UserAdministrationService users) { this.users = users; }
    @GetMapping @PreAuthorize("hasAuthority('system:user:read')")
    public PageResponse<AdminUserResponse> list(@RequestParam(required = false) String q, @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return users.list(q, status, page, size); }
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('system:user:read')")
    public AdminUserResponse get(@PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}") String id) { return users.get(id); }
    @PatchMapping("/{id}/status") @PreAuthorize("hasAuthority('system:user:write')")
    public AdminUserResponse updateStatus(@PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}") String id, @Valid @RequestBody UpdateUserStatusRequest request) { return users.changeStatus(id, request.status()); }
    @PostMapping("/{id}/sessions/revoke") @PreAuthorize("hasAuthority('system:user:write')")
    public org.springframework.http.ResponseEntity<Void> revokeSessions(@PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}") String id) { users.revokeSessions(id); return org.springframework.http.ResponseEntity.noContent().build(); }
}
