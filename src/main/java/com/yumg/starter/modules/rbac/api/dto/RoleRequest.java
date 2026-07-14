package com.yumg.starter.modules.rbac.api.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record RoleRequest(@NotBlank @Pattern(regexp="[A-Z_]{2,64}") String code, @NotBlank @Size(max=160) String displayName) { }
