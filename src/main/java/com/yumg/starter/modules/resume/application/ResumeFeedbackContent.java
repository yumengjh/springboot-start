package com.yumg.starter.modules.resume.application;

import com.yumg.starter.entities.ResumeFeedback;
import java.time.Instant;

public record ResumeFeedbackContent(String id, ResumeFeedbackType type, ResumeReaction reaction,
                                    Integer rating, String suggestion, String ipAddress,
                                    String userAgent, ResumeFeedbackStatus status, Instant createdAt) {
    public static ResumeFeedbackContent from(ResumeFeedback feedback) {
        return new ResumeFeedbackContent(feedback.getId(), feedback.getType(), feedback.getReaction(),
                feedback.getRating(), feedback.getSuggestion(), feedback.getIpAddress(), feedback.getUserAgent(),
                feedback.getStatus(), feedback.getCreatedAt());
    }
}
