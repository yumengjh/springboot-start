package com.yumg.starter.modules.security.api;

import com.yumg.starter.common.api.PageResponse;
import com.yumg.starter.modules.security.api.dto.AuditEventResponse;
import com.yumg.starter.modules.security.application.AuditService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@Validated
@RequestMapping("/api/v1/system/audit-events")
@Tag(name = "系统审计") @SecurityRequirement(name = "bearerAuth")
public class AuditEventController {
    private final AuditService audit;
    public AuditEventController(AuditService audit) { this.audit = audit; }

    @GetMapping
    @PreAuthorize("hasAuthority('system:audit:read')")
    public PageResponse<AuditEventResponse> list(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                 @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return audit.list(page, size);
    }
}
