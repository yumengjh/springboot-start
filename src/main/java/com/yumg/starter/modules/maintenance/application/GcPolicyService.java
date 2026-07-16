package com.yumg.starter.modules.maintenance.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.MaintenanceGcLock;
import com.yumg.starter.entities.MaintenanceGcPolicy;
import com.yumg.starter.modules.maintenance.infrastructure.MaintenanceGcLockRepository;
import com.yumg.starter.modules.maintenance.infrastructure.MaintenanceGcPolicyRepository;
import com.yumg.starter.modules.security.application.AuditService;
import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GcPolicyService {
    private final MaintenanceGcPolicyRepository policies; private final MaintenanceGcLockRepository locks;
    private final Map<String, GcResource> resources; private final AuditService audit;
    public GcPolicyService(MaintenanceGcPolicyRepository policies, MaintenanceGcLockRepository locks,
                           List<GcResource> resources, AuditService audit) {
        this.policies = policies; this.locks = locks;
        this.resources = resources.stream().collect(java.util.stream.Collectors.toMap(item -> item.descriptor().code(), Function.identity()));
        this.audit = audit;
    }
    @PostConstruct @Transactional void initialize() {
        locks.findById("global").orElseGet(() -> locks.save(new MaintenanceGcLock("global")));
        resources.values().forEach(resource -> policies.findByResourceCode(resource.descriptor().code())
                .orElseGet(() -> policies.save(new MaintenanceGcPolicy(resource.descriptor().code(), true,
                        resource.descriptor().automaticByDefault(), resource.descriptor().defaultRetentionDays(), resource.descriptor().defaultBatchSize()))));
    }
    @Transactional(readOnly = true) public List<GcPolicyContent> list() {
        return policies.findAll().stream().filter(policy -> resources.containsKey(policy.getResourceCode()))
                .map(policy -> GcPolicyContent.from(policy, resources.get(policy.getResourceCode()).descriptor()))
                .sorted(Comparator.comparing(GcPolicyContent::resourceCode)).toList();
    }
    @Transactional(readOnly = true) public MaintenanceGcPolicy policy(String code) { return policies.findByResourceCode(code).orElseThrow(ApiException::notFound); }
    @Transactional public GcPolicyContent update(String code, boolean enabled, boolean automaticEnabled, int retentionDays, int batchSize) {
        if (!resources.containsKey(code) || retentionDays < 0 || retentionDays > 3650 || batchSize < 1 || batchSize > 5000) throw ApiException.invalidParameter();
        MaintenanceGcPolicy policy = policy(code); policy.change(enabled, automaticEnabled, retentionDays, batchSize);
        audit.event("GC_POLICY_UPDATED", "MaintenanceGcPolicy", policy.getId());
        return GcPolicyContent.from(policy, resources.get(code).descriptor());
    }
    Map<String, GcResource> resources() { return resources; }
}
