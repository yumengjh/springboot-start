package com.yumg.starter.modules.navigation.api.dto;

import com.yumg.starter.entities.NavigationMenuType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NavigationMenuRequest(
        @Pattern(regexp = "[0-9a-fA-F-]{36}") String parentId,
        @NotBlank @Pattern(regexp = "[a-z][a-z0-9-]{1,99}") String code,
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Pattern(regexp = "/[A-Za-z0-9/_-]*") @Size(max = 200) String routePath,
        @Size(max = 100) String componentKey,
        @Size(max = 100) String icon,
        int sortOrder,
        @NotNull NavigationMenuType menuType,
        @Pattern(regexp = "|[a-z]+:[a-z-]+:[a-z]+") String requiredPermission,
        boolean visible,
        boolean enabled,
        boolean keepAlive) {
}
