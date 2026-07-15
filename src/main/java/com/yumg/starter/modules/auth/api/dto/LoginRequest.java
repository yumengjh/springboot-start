package com.yumg.starter.modules.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Size(max = 32) String username,
                           @NotBlank @Size(max = 128) String password,
                           boolean rememberMe) {
}
