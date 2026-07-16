package com.yumg.starter.modules.maintenance.api.dto;
import jakarta.validation.constraints.Size;
import java.util.List;
public record GcRunRequest(boolean dryRun, @Size(max = 20) List<@Size(min = 1, max = 100) String> resourceCodes) {}
