package com.yumg.starter.modules.maintenance.application;
import com.yumg.starter.entities.MaintenanceGcPolicy;
import com.yumg.starter.modules.security.application.RateLimitService;
import java.time.Instant;
import org.springframework.stereotype.Component;
@Component public class RateLimitStateGcResource implements GcResource {
    private final RateLimitService limits; public RateLimitStateGcResource(RateLimitService limits) { this.limits = limits; }
    @Override public GcResourceDescriptor descriptor() { return new GcResourceDescriptor("rate-limit-state", "限流内存状态", "移除已过期的限流窗口，避免不同客户端持续占用内存。", 0, 1000, true); }
    @Override public GcResourceResult execute(MaintenanceGcPolicy policy, boolean dryRun, Instant now) { long candidates = limits.expiredStateCount(now); long deleted = dryRun ? 0 : limits.cleanupExpired(now); return new GcResourceResult(descriptor().code(), candidates, deleted, dryRun ? "预览" : "已清理过期窗口"); }
}
