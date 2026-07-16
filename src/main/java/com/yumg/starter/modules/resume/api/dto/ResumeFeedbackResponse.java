package com.yumg.starter.modules.resume.api.dto;

import com.yumg.starter.modules.resume.application.ResumeFeedbackContent;
import com.yumg.starter.modules.resume.application.ResumeFeedbackStatus;
import com.yumg.starter.modules.resume.application.ResumeFeedbackType;
import com.yumg.starter.modules.resume.application.ResumeReaction;
import java.time.Instant;

public record ResumeFeedbackResponse(String id, ResumeFeedbackType type, ResumeReaction reaction,
                                     Integer rating, String suggestion, String ipAddress,
                                     String userAgent, ResumeFeedbackStatus status, Instant createdAt) {
    public static ResumeFeedbackResponse from(ResumeFeedbackContent content) {
        return new ResumeFeedbackResponse(content.id(), content.type(), content.reaction(), content.rating(),
                content.suggestion(), content.ipAddress(), content.userAgent(), content.status(), content.createdAt());
    }
}
