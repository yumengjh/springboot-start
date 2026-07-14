package com.yumg.starter.modules.security.api.dto;

import com.yumg.starter.entities.IpAccessRule.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record IpAccessRuleRequest(@NotNull Type type, @NotBlank @Pattern(regexp = "[0-9a-fA-F:.]+(?:/[0-9]{1,3})?") String network,
                                  Long expiresAt, @NotBlank String reason) { }
