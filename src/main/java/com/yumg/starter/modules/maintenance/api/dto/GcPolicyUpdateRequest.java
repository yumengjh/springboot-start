package com.yumg.starter.modules.maintenance.api.dto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
public record GcPolicyUpdateRequest(@NotNull Boolean enabled, @NotNull Boolean automaticEnabled,
                                    @Min(0) @Max(3650) Integer retentionDays,
                                    @Min(1) @Max(5000) Integer batchSize) {}
