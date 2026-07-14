package com.yumg.starter.modules.runtimeconfig.api.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateRuntimeSettingRequest(@NotNull String value) {}
