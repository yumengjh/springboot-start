package com.yumg.starter.entities;

import com.yumg.starter.common.entity.AuditedEntity;
import com.yumg.starter.modules.resume.application.ResumeFeedbackStatus;
import com.yumg.starter.modules.resume.application.ResumeFeedbackType;
import com.yumg.starter.modules.resume.application.ResumeReaction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "resume_feedbacks")
public class ResumeFeedback extends AuditedEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResumeFeedbackType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ResumeReaction reaction;

    private Integer rating;

    @Column(columnDefinition = "text")
    private String suggestion;

    @Column(name = "ip_address", nullable = false, length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResumeFeedbackStatus status;

    protected ResumeFeedback() {
    }

    private ResumeFeedback(ResumeFeedbackType type, ResumeReaction reaction, Integer rating,
                           String suggestion, String ipAddress, String userAgent) {
        this.type = type;
        this.reaction = reaction;
        this.rating = rating;
        this.suggestion = suggestion;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.status = ResumeFeedbackStatus.UNREAD;
    }

    public static ResumeFeedback reaction(ResumeReaction reaction, String ipAddress, String userAgent) {
        return new ResumeFeedback(ResumeFeedbackType.REACTION, reaction, null, null, ipAddress, userAgent);
    }

    public static ResumeFeedback review(int rating, String suggestion, String ipAddress, String userAgent) {
        return new ResumeFeedback(ResumeFeedbackType.REVIEW, null, rating, suggestion, ipAddress, userAgent);
    }

    public ResumeFeedbackType getType() { return type; }
    public ResumeReaction getReaction() { return reaction; }
    public Integer getRating() { return rating; }
    public String getSuggestion() { return suggestion; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public ResumeFeedbackStatus getStatus() { return status; }

    public void changeStatus(ResumeFeedbackStatus status) {
        this.status = status;
    }
}
