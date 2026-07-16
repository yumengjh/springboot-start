package com.yumg.starter.modules.maintenance.application;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;

@Component
public class GcScheduler {
    private final GcExecutionService execution; private final RuntimeSettingService settings;
    private final AtomicLong lastRunAt = new AtomicLong();
    public GcScheduler(GcExecutionService execution, RuntimeSettingService settings) { this.execution = execution; this.settings = settings; }
    @Scheduled(fixedDelay = 60000)
    public void runWhenDue() {
        if (!settings.enabled("maintenance.gc.enabled") || !settings.enabled("maintenance.gc.schedule.enabled")) return;
        long now = Instant.now().toEpochMilli(); long interval = settings.integer("maintenance.gc.interval-minutes") * 60_000L;
        if (now - lastRunAt.get() < interval || !lastRunAt.compareAndSet(lastRunAt.get(), now)) return;
        try { execution.run("SCHEDULED", false, List.of(), null); } catch (RuntimeException ignored) { }
    }
}
