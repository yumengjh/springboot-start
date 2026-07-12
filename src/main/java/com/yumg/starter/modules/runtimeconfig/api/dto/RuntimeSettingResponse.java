package com.yumg.starter.modules.runtimeconfig.api.dto;

import com.yumg.starter.entities.RuntimeSetting;

public record RuntimeSettingResponse(String key, String type, String value) {
    public static RuntimeSettingResponse from(RuntimeSetting setting) {
        return new RuntimeSettingResponse(setting.getKey(), setting.getValueType(), setting.getValue());
    }
}
