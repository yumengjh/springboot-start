package com.yumg.starter.modules.security.application;

import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class BruteForceService {
    private final RuntimeSettingService settings;
    private final ConcurrentHashMap<String, Failures> failures = new ConcurrentHashMap<>();
    public BruteForceService(RuntimeSettingService settings) { this.settings = settings; }
    public boolean recordFailure(String username) {
        if (!settings.enabled("security.brute-force.enabled")) return false;
        long now = Instant.now().toEpochMilli(); int threshold = settings.integer("security.brute-force.failure-threshold");
        Failures value = failures.compute(username, (ignored, current) -> {
            if (current == null || now - current.startedAt >= settings.integer("security.brute-force.window-seconds") * 1000L) return new Failures(now, 1);
            return new Failures(current.startedAt, current.count + 1);
        });
        return value.count >= threshold;
    }
    public void clear(String username) { failures.remove(username); }
    public Instant lockUntil() { return Instant.now().plusSeconds(settings.integer("security.brute-force.lock-seconds")); }
    private record Failures(long startedAt, int count) {}
}
