package com.yumg.starter.modules.navigation.api.dto;

import jakarta.validation.constraints.NotNull;

public record NavigationMenuEnabledRequest(@NotNull Boolean enabled) {
}
