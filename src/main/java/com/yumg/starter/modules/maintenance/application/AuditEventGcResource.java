package com.yumg.starter.modules.maintenance.application;
import com.yumg.starter.entities.MaintenanceGcPolicy;
import com.yumg.starter.modules.security.infrastructure.AuditEventRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
@Component public class AuditEventGcResource implements GcResource {
    private final AuditEventRepository events;
    public AuditEventGcResource(AuditEventRepository events) { this.events = events; }
    @Override public GcResourceDescriptor descriptor() { return new GcResourceDescriptor("audit-events", "审计日志", "清理超过保留期的系统审计记录。", 180, 500, true); }
    @Override public GcResourceResult execute(MaintenanceGcPolicy policy, boolean dryRun, Instant now) {
        if (policy.getRetentionDays() == 0) return new GcResourceResult(descriptor().code(), 0, 0, "保留期为 0，已跳过");
        Instant cutoff = now.minus(java.time.Duration.ofDays(policy.getRetentionDays())); long candidates = events.countByOccurredAtBefore(cutoff);
        return new GcResourceResult(descriptor().code(), candidates, dryRun ? 0 : events.deleteByOccurredAtBefore(cutoff), dryRun ? "预览" : "已清理");
    }
}
