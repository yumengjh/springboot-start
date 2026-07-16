package com.yumg.starter.modules.maintenance.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.common.web.TraceIdFilter;
import com.yumg.starter.entities.MaintenanceGcPolicy;
import com.yumg.starter.entities.MaintenanceGcRun;
import com.yumg.starter.modules.maintenance.infrastructure.MaintenanceGcLockRepository;
import com.yumg.starter.modules.maintenance.infrastructure.MaintenanceGcRunRepository;
import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import com.yumg.starter.modules.security.application.AuditService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.MDC;
import com.yumg.starter.common.api.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GcExecutionService {
    private static final String LOCK_NAME = "global";
    private final GcPolicyService policies; private final MaintenanceGcRunRepository runs;
    private final MaintenanceGcLockRepository locks; private final RuntimeSettingService settings;
    private final AuditService audit;
    private final GcRunSummaryCodec summaries;
    public GcExecutionService(GcPolicyService policies, MaintenanceGcRunRepository runs,
                              MaintenanceGcLockRepository locks, RuntimeSettingService settings, AuditService audit,
                              GcRunSummaryCodec summaries) {
        this.policies = policies; this.runs = runs; this.locks = locks; this.settings = settings; this.audit = audit; this.summaries = summaries;
    }
    @Transactional public GcRunContent run(String triggerType, boolean dryRun, List<String> requestedResources, String requestedBy) {
        if (!settings.enabled("maintenance.gc.enabled")) throw ApiException.notFound();
        Map<String, GcResource> resources = policies.resources();
        Set<String> selected = requestedResources == null || requestedResources.isEmpty() ? resources.keySet() : Set.copyOf(requestedResources);
        if (!resources.keySet().containsAll(selected)) throw ApiException.invalidParameter();
        MaintenanceGcRun run = runs.save(new MaintenanceGcRun(triggerType, dryRun, requestedBy, String.join(",", selected),
                MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)));
        Instant now = Instant.now();
        if (locks.tryAcquire(LOCK_NAME, run.getId(), now, now.plusSeconds(settings.integer("maintenance.gc.max-run-seconds"))) != 1) {
            run.skip("已有 GC 任务正在运行"); return GcRunContent.from(run, summaries);
        }
        try {
            List<GcResourceResult> results = new ArrayList<>();
            for (String code : selected) {
                MaintenanceGcPolicy policy = policies.policy(code);
                if (!policy.isEnabled() || ("SCHEDULED".equals(triggerType) && !policy.isAutomaticEnabled())) continue;
                results.add(resources.get(code).execute(policy, dryRun, now));
            }
            run.complete(summaries.write(GcRunSummary.from(results)));
            audit.event("GC_RUN_COMPLETED", "MaintenanceGcRun", run.getId(), "SUCCESS", "{\"dryRun\":" + dryRun + "}");
        } catch (RuntimeException exception) {
            run.fail(exception.getMessage()); audit.event("GC_RUN_FAILED", "MaintenanceGcRun", run.getId(), "FAILURE", null); throw exception;
        } finally {
            locks.release(LOCK_NAME, run.getId(), Instant.now());
        }
        return GcRunContent.from(run, summaries);
    }
    @Transactional(readOnly = true) public PageResponse<GcRunContent> history(int page, int size) {
        return PageResponse.from(runs.findAll(org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("startedAt").descending())).map(run -> GcRunContent.from(run, summaries)));
    }
}
