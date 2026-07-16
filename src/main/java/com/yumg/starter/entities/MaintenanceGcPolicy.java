package com.yumg.starter.entities;

import com.yumg.starter.common.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "maintenance_gc_policies")
public class MaintenanceGcPolicy extends AuditedEntity {
    @Column(name = "resource_code", nullable = false, unique = true, length = 100)
    private String resourceCode;
    @Column(nullable = false) private boolean enabled;
    @Column(name = "automatic_enabled", nullable = false) private boolean automaticEnabled;
    @Column(name = "retention_days", nullable = false) private int retentionDays;
    @Column(name = "batch_size", nullable = false) private int batchSize;

    protected MaintenanceGcPolicy() {}
    public MaintenanceGcPolicy(String resourceCode, boolean enabled, boolean automaticEnabled,
                               int retentionDays, int batchSize) {
        this.resourceCode = resourceCode; this.enabled = enabled; this.automaticEnabled = automaticEnabled;
        this.retentionDays = retentionDays; this.batchSize = batchSize;
    }
    public String getResourceCode() { return resourceCode; }
    public boolean isEnabled() { return enabled; }
    public boolean isAutomaticEnabled() { return automaticEnabled; }
    public int getRetentionDays() { return retentionDays; }
    public int getBatchSize() { return batchSize; }
    public void change(boolean enabled, boolean automaticEnabled, int retentionDays, int batchSize) {
        this.enabled = enabled; this.automaticEnabled = automaticEnabled;
        this.retentionDays = retentionDays; this.batchSize = batchSize;
    }
}
