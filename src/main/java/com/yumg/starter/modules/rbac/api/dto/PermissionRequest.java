package com.yumg.starter.modules.rbac.api.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record PermissionRequest(@NotBlank @Pattern(regexp="[a-z]+:[a-z-]+:[a-z]+") String code, @Size(max=500) String description) { }
