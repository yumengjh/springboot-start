package com.yumg.starter.modules.runtimeconfig.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.RuntimeSetting;
import com.yumg.starter.modules.runtimeconfig.api.dto.RuntimeSettingResponse;
import com.yumg.starter.modules.runtimeconfig.infrastructure.RuntimeSettingRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.yumg.starter.modules.security.application.AuditService;

@Service
public class RuntimeSettingService {
    private static final Map<String, Definition> DEFINITIONS = Map.of(
            "security.rate-limit.enabled", new Definition("BOOLEAN", "true", 0, 0),
            "security.rate-limit.capacity", new Definition("INTEGER", "120", 1, 10000),
            "security.rate-limit.window-seconds", new Definition("INTEGER", "60", 1, 3600),
            "security.brute-force.enabled", new Definition("BOOLEAN", "true", 0, 0),
            "security.brute-force.failure-threshold", new Definition("INTEGER", "5", 3, 20),
            "security.brute-force.lock-seconds", new Definition("INTEGER", "900", 60, 86400),
            "security.audit.enabled", new Definition("BOOLEAN", "true", 0, 0),
            "security.cors.allowed-origins", new Definition("STRING", "*", 0, 0),
            "security.cors.allowed-methods", new Definition("STRING", "GET,POST,PUT,PATCH,DELETE,OPTIONS", 0, 0),
            "security.endpoint.disabled-patterns", new Definition("STRING", "", 0, 0));
    private final RuntimeSettingRepository settings;
    private final ConcurrentHashMap<String, String> snapshot = new ConcurrentHashMap<>();
    private final AuditService audit;

    public RuntimeSettingService(RuntimeSettingRepository settings) { this(settings, null); }
    @Autowired public RuntimeSettingService(RuntimeSettingRepository settings, AuditService audit) { this.settings = settings; this.audit = audit; }

    @PostConstruct
    @Transactional
    void initialize() {
        DEFINITIONS.forEach((key, definition) -> settings.findByKey(key).ifPresentOrElse(setting ->
                snapshot.put(key, setting.getValue()), () -> {
                    RuntimeSetting created = settings.save(new RuntimeSetting(key, definition.type(), definition.defaultValue()));
                    snapshot.put(key, created.getValue());
                }));
    }

    @Transactional(readOnly = true)
    public List<RuntimeSettingResponse> list() {
        return settings.findAll().stream().map(RuntimeSettingResponse::from).toList();
    }

    @Transactional
    public RuntimeSettingResponse update(String key, String value) {
        Definition definition = DEFINITIONS.get(key);
        if (definition == null || !valid(definition, value)) throw ApiException.notFound();
        RuntimeSetting setting = settings.findByKey(key).orElseThrow(ApiException::notFound);
        setting.changeValue(value);
        snapshot.put(key, value);
        if (audit != null && enabled("security.audit.enabled")) audit.runtimeSettingChanged(key, value);
        return RuntimeSettingResponse.from(setting);
    }

    public boolean enabled(String key) { return Boolean.parseBoolean(snapshot.get(key)); }
    public int integer(String key) { return Integer.parseInt(snapshot.get(key)); }
    public String string(String key) { return snapshot.get(key); }

    private boolean valid(Definition definition, String value) {
        if ("STRING".equals(definition.type())) return value != null && value.length() <= 1000;
        if ("BOOLEAN".equals(definition.type())) return "true".equals(value) || "false".equals(value);
        try { int number = Integer.parseInt(value); return number >= definition.min() && number <= definition.max(); }
        catch (NumberFormatException ignored) { return false; }
    }
    private record Definition(String type, String defaultValue, int min, int max) {}
}
