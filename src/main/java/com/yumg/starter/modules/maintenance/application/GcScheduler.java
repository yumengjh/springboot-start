package com.yumg.starter.modules.maintenance.application;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;

@Component
public class GcScheduler {
    private static final Logger log = LoggerFactory.getLogger(GcScheduler.class);
    private final GcExecutionService execution; private final RuntimeSettingService settings;
    private final AtomicLong lastRunAt = new AtomicLong();
    private volatile GcSchedulerStatus status = GcSchedulerStatus.initial();
    public GcScheduler(GcExecutionService execution, RuntimeSettingService settings) { this.execution = execution; this.settings = settings; }
    @Scheduled(fixedDelay = 60000)
    public void runWhenDue() {
        if (!settings.enabled("maintenance.gc.enabled") || !settings.enabled("maintenance.gc.schedule.enabled")) return;
        long now = Instant.now().toEpochMilli(); long interval = settings.integer("maintenance.gc.interval-minutes") * 60_000L;
        long previous = lastRunAt.get();
        if (now - previous < interval || !lastRunAt.compareAndSet(previous, now)) return;
        Instant attemptedAt = Instant.ofEpochMilli(now);
        try {
            execution.run("SCHEDULED", false, List.of(), null);
            status = new GcSchedulerStatus(attemptedAt, attemptedAt, status.lastFailedAt(), 0, null,
                    Instant.ofEpochMilli(now + interval));
        } catch (RuntimeException exception) {
            int failures = status.consecutiveFailures() + 1;
            String detail = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            status = new GcSchedulerStatus(attemptedAt, status.lastSucceededAt(), attemptedAt, failures, detail,
                    Instant.ofEpochMilli(now + interval));
            log.warn("Scheduled GC run failed; consecutiveFailures={}, traceable run record was retained", failures, exception);
        }
    }
    public GcSchedulerStatus status() { return status; }
}
