package com.yumg.starter.modules.maintenance.application;
import com.yumg.starter.entities.MaintenanceGcPolicy;
public record GcPolicyContent(String resourceCode, String displayName, String description, boolean enabled,
                              boolean automaticEnabled, int retentionDays, int batchSize,
                              boolean automaticByDefault) {
    static GcPolicyContent from(MaintenanceGcPolicy policy, GcResourceDescriptor descriptor) {
        return new GcPolicyContent(policy.getResourceCode(), descriptor.displayName(), descriptor.description(),
                policy.isEnabled(), policy.isAutomaticEnabled(), policy.getRetentionDays(), policy.getBatchSize(),
                descriptor.automaticByDefault());
    }
}
