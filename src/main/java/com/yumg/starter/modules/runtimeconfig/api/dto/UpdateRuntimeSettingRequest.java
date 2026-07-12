package com.yumg.starter.modules.runtimeconfig.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateRuntimeSettingRequest(@NotBlank String value) {}
