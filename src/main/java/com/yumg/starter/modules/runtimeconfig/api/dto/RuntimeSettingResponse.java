package com.yumg.starter.modules.runtimeconfig.api.dto;

import com.yumg.starter.entities.RuntimeSetting;

public record RuntimeSettingResponse(String key, String displayName, String description,
                                     String type, String value) {
    public static RuntimeSettingResponse from(RuntimeSetting setting, String displayName,
                                              String description) {
        return new RuntimeSettingResponse(setting.getKey(), displayName, description,
                setting.getValueType(), setting.getValue());
    }
}
