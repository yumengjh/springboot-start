package com.yumg.starter.modules.maintenance.application;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("gcSchedulerHealth")
public class GcSchedulerHealthIndicator implements HealthIndicator {
    private final GcScheduler scheduler;
    public GcSchedulerHealthIndicator(GcScheduler scheduler) { this.scheduler = scheduler; }
    @Override public Health health() {
        GcSchedulerStatus status = scheduler.status();
        Health.Builder result = status.consecutiveFailures() >= 3 ? Health.down() : Health.up();
        result.withDetail("consecutiveFailures", status.consecutiveFailures());
        if (status.lastSucceededAt() != null) result.withDetail("lastSucceededAt", status.lastSucceededAt());
        if (status.lastFailedAt() != null) result.withDetail("lastFailedAt", status.lastFailedAt());
        return result.build();
    }
}
