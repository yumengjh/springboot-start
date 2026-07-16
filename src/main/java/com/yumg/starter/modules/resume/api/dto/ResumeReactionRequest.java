package com.yumg.starter.modules.resume.api.dto;

import com.yumg.starter.modules.resume.application.ResumeReaction;
import jakarta.validation.constraints.NotNull;

public record ResumeReactionRequest(@NotNull ResumeReaction reaction) {
}
