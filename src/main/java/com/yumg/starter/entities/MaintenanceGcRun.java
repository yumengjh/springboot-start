package com.yumg.starter.entities;

import com.yumg.starter.common.entity.BaseUuidEntity;
import com.yumg.starter.common.entity.InstantStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "maintenance_gc_runs")
public class MaintenanceGcRun extends BaseUuidEntity {
    @Column(nullable = false, length = 32) private String status;
    @Column(name = "trigger_type", nullable = false, length = 32) private String triggerType;
    @Column(name = "dry_run", nullable = false) private boolean dryRun;
    @Column(name = "requested_by", length = 36) private String requestedBy;
    @Column(name = "resource_codes", columnDefinition = "text") private String resourceCodes;
    @Column(name = "summary_json", columnDefinition = "text") private String summary;
    @Column(name = "error_message", length = 1000) private String errorMessage;
    @Column(name = "trace_id", length = 100) private String traceId;
    @Column(name = "started_at", nullable = false) @Convert(converter = InstantStringConverter.class) private Instant startedAt;
    @Column(name = "completed_at") @Convert(converter = InstantStringConverter.class) private Instant completedAt;
    protected MaintenanceGcRun() {}
    public MaintenanceGcRun(String triggerType, boolean dryRun, String requestedBy, String resourceCodes, String traceId) {
        this.status = "RUNNING"; this.triggerType = triggerType; this.dryRun = dryRun; this.requestedBy = requestedBy;
        this.resourceCodes = resourceCodes; this.traceId = traceId; this.startedAt = Instant.now();
    }
    public String getStatus() { return status; } public String getTriggerType() { return triggerType; }
    public boolean isDryRun() { return dryRun; } public String getRequestedBy() { return requestedBy; }
    public String getResourceCodes() { return resourceCodes; } public String getSummary() { return summary; }
    public String getErrorMessage() { return errorMessage; } public String getTraceId() { return traceId; }
    public Instant getStartedAt() { return startedAt; } public Instant getCompletedAt() { return completedAt; }
    public void complete(String summary) { this.status = "SUCCEEDED"; this.summary = summary; this.completedAt = Instant.now(); }
    public void skip(String summary) { this.status = "SKIPPED"; this.summary = summary; this.completedAt = Instant.now(); }
    public void fail(String message) { this.status = "FAILED"; this.errorMessage = message == null ? "Unknown error" : message.substring(0, Math.min(message.length(), 1000)); this.completedAt = Instant.now(); }
}
