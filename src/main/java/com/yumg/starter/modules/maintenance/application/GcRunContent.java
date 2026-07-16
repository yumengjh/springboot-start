package com.yumg.starter.modules.maintenance.application;

import com.yumg.starter.entities.MaintenanceGcRun;
import java.time.Instant;

public record GcRunContent(String id, String status, String triggerType, boolean dryRun, String requestedBy,
                           String resourceCodes, GcRunSummary summary, String errorMessage, String traceId,
                           Instant startedAt, Instant completedAt) {
    public static GcRunContent from(MaintenanceGcRun run, GcRunSummaryCodec summaryCodec) {
        return new GcRunContent(run.getId(), run.getStatus(), run.getTriggerType(), run.isDryRun(),
                run.getRequestedBy(), run.getResourceCodes(), summaryCodec.read(run.getSummary()), run.getErrorMessage(),
                run.getTraceId(), run.getStartedAt(), run.getCompletedAt());
    }
}
