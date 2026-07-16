package com.yumg.starter.modules.resume.api.dto;

import com.yumg.starter.modules.resume.application.ResumeFeedbackStatus;
import jakarta.validation.constraints.NotNull;

public record ResumeFeedbackStatusRequest(@NotNull ResumeFeedbackStatus status) {
}
