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
            long duration = settings.integer("security.brute-force.window-seconds") * 1000L;
            if (current == null || now >= current.expiresAt) return new Failures(now, now + duration, 1);
            return new Failures(current.startedAt, current.expiresAt, current.count + 1);
        });
        return value.count >= threshold;
    }
    public void clear(String username) { failures.remove(username); }
    public Instant lockUntil() { return Instant.now().plusSeconds(settings.integer("security.brute-force.lock-seconds")); }
    public long cleanupExpired(Instant referenceTime) {
        long now = referenceTime.toEpochMilli(); int before = failures.size();
        failures.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
        return before - failures.size();
    }
    public long expiredStateCount(Instant referenceTime) {
        long now = referenceTime.toEpochMilli();
        return failures.values().stream().filter(value -> value.expiresAt <= now).count();
    }
    public int stateCount() { return failures.size(); }
    private record Failures(long startedAt, long expiresAt, int count) {}
}
