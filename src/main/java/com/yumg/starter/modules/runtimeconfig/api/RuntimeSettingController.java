package com.yumg.starter.modules.runtimeconfig.api;

import com.yumg.starter.modules.runtimeconfig.api.dto.RuntimeSettingResponse;
import com.yumg.starter.modules.runtimeconfig.api.dto.UpdateRuntimeSettingRequest;
import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/runtime-config")
public class RuntimeSettingController {
    private final RuntimeSettingService settings;
    public RuntimeSettingController(RuntimeSettingService settings) { this.settings = settings; }
    @GetMapping public List<RuntimeSettingResponse> list() { return settings.list(); }
    @PutMapping("/{key:.+}")
    public RuntimeSettingResponse update(@PathVariable String key,
                                         @Valid @RequestBody UpdateRuntimeSettingRequest request) {
        return settings.update(key, request.value());
    }
}
