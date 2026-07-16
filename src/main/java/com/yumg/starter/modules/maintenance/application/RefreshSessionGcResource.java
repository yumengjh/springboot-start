package com.yumg.starter.modules.maintenance.application;
import com.yumg.starter.entities.MaintenanceGcPolicy;
import com.yumg.starter.modules.auth.infrastructure.RefreshSessionRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
@Component public class RefreshSessionGcResource implements GcResource {
    private final RefreshSessionRepository sessions;
    public RefreshSessionGcResource(RefreshSessionRepository sessions) { this.sessions = sessions; }
    @Override public GcResourceDescriptor descriptor() { return new GcResourceDescriptor("refresh-sessions", "Refresh Token 会话", "清理已过期或已撤销的刷新令牌会话。", 30, 500, true); }
    @Override public GcResourceResult execute(MaintenanceGcPolicy policy, boolean dryRun, Instant now) {
        if (policy.getRetentionDays() == 0) return new GcResourceResult(descriptor().code(), 0, 0, "保留期为 0，已跳过");
        Instant cutoff = now.minus(java.time.Duration.ofDays(policy.getRetentionDays())); long candidates = sessions.countRetiredBefore(cutoff);
        return new GcResourceResult(descriptor().code(), candidates, dryRun ? 0 : sessions.deleteRetiredBefore(cutoff), dryRun ? "预览" : "已清理");
    }
}
