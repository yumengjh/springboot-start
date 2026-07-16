package com.yumg.starter.modules.security.application;

import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    private final RuntimeSettingService settings;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    public RateLimitService(RuntimeSettingService settings) { this.settings = settings; }
    public boolean allow(String key) {
        if (!settings.enabled("security.rate-limit.enabled")) return true;
        return allow(key, settings.integer("security.rate-limit.capacity"), settings.integer("security.rate-limit.window-seconds"));
    }

    public boolean allow(String key, int capacity, int windowSeconds) {
        long now = Instant.now().toEpochMilli();
        long duration = windowSeconds * 1000L;
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || now >= current.expiresAt) return new Window(now, now + duration, 1, true);
            if (current.count >= capacity) return new Window(current.startedAt, current.expiresAt, current.count, false);
            return new Window(current.startedAt, current.expiresAt, current.count + 1, true);
        });
        return window.allowed;
    }
    public long cleanupExpired(Instant referenceTime) {
        long now = referenceTime.toEpochMilli(); int before = windows.size();
        windows.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
        return before - windows.size();
    }
    public long expiredStateCount(Instant referenceTime) {
        long now = referenceTime.toEpochMilli();
        return windows.values().stream().filter(window -> window.expiresAt <= now).count();
    }
    public int stateCount() { return windows.size(); }
    private record Window(long startedAt, long expiresAt, int count, boolean allowed) {}
}
