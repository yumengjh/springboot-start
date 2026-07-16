package com.yumg.starter.modules.maintenance.application;
import com.yumg.starter.entities.MaintenanceGcPolicy;
import com.yumg.starter.modules.security.application.BruteForceService;
import java.time.Instant;
import org.springframework.stereotype.Component;
@Component public class BruteForceStateGcResource implements GcResource {
    private final BruteForceService bruteForce; public BruteForceStateGcResource(BruteForceService bruteForce) { this.bruteForce = bruteForce; }
    @Override public GcResourceDescriptor descriptor() { return new GcResourceDescriptor("brute-force-state", "登录失败内存状态", "移除已过期的登录失败计数，避免无效账号持续占用内存。", 0, 1000, true); }
    @Override public GcResourceResult execute(MaintenanceGcPolicy policy, boolean dryRun, Instant now) { long candidates = bruteForce.expiredStateCount(now); long deleted = dryRun ? 0 : bruteForce.cleanupExpired(now); return new GcResourceResult(descriptor().code(), candidates, deleted, dryRun ? "预览" : "已清理过期窗口"); }
}
