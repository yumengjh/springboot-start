package com.yumg.starter.modules.users.api.dto;

import com.yumg.starter.entities.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull UserStatus status) {}
