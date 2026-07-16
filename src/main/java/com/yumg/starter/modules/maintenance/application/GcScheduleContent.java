package com.yumg.starter.modules.maintenance.application;
import java.time.Instant;

/** Global controls for automatic GC. Manual runs remain available while this is disabled. */
public record GcScheduleContent(boolean automaticEnabled, int intervalMinutes, Instant lastAttemptedAt,
                                Instant lastSucceededAt, Instant lastFailedAt, int consecutiveFailures,
                                String lastFailureMessage, Instant nextEligibleAt) {}
