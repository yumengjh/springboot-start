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
            if (current == null || now - current.startedAt >= duration) return new Window(now, 1, true);
            if (current.count >= capacity) return new Window(current.startedAt, current.count, false);
            return new Window(current.startedAt, current.count + 1, true);
        });
        return window.allowed;
    }
    private record Window(long startedAt, int count, boolean allowed) {}
}
