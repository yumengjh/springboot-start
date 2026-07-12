package com.yumg.starter.modules.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_.-]{3,32}") String username,
        @NotBlank @Size(max = 80) String displayName,
        @NotBlank @Size(min = 10, max = 128) String password) {
}
