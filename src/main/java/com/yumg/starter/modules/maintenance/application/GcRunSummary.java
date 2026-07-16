package com.yumg.starter.modules.maintenance.application;

import java.util.List;

/** Structured, persisted run summary. Never expose an implementation-specific compact string to clients. */
public record GcRunSummary(long totalCandidates, long totalDeleted, List<GcRunResourceContent> resources) {
    public GcRunSummary {
        resources = resources == null ? List.of() : List.copyOf(resources);
    }

    static GcRunSummary from(List<GcResourceResult> results) {
        List<GcRunResourceContent> resources = results.stream().map(GcRunResourceContent::from).toList();
        return new GcRunSummary(
                resources.stream().mapToLong(GcRunResourceContent::candidates).sum(),
                resources.stream().mapToLong(GcRunResourceContent::deleted).sum(), resources);
    }
}
