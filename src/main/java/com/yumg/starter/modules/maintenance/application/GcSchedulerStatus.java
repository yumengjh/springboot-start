package com.yumg.starter.modules.maintenance.application;

import java.time.Instant;

public record GcSchedulerStatus(Instant lastAttemptedAt, Instant lastSucceededAt, Instant lastFailedAt,
                                int consecutiveFailures, String lastFailureMessage, Instant nextEligibleAt) {
    static GcSchedulerStatus initial() { return new GcSchedulerStatus(null, null, null, 0, null, null); }
}
