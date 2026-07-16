package com.yumg.starter.modules.resume.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.common.api.PageResponse;
import com.yumg.starter.entities.ResumeFeedback;
import com.yumg.starter.modules.resume.infrastructure.ResumeFeedbackRepository;
import com.yumg.starter.modules.security.application.AuditService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeFeedbackService {

    private final ResumeFeedbackRepository feedbacks;
    private final AuditService audit;

    public ResumeFeedbackService(ResumeFeedbackRepository feedbacks, AuditService audit) {
        this.feedbacks = feedbacks;
        this.audit = audit;
    }

    @Transactional
    public ResumeFeedbackContent submitReaction(ResumeReaction reaction, String ipAddress, String userAgent) {
        if (reaction == null) throw ApiException.invalidParameter();
        return save(ResumeFeedback.reaction(reaction, ipAddress, truncateUserAgent(userAgent)));
    }

    @Transactional
    public ResumeFeedbackContent submitReview(int rating, String suggestion, String ipAddress, String userAgent) {
        if (rating < 1 || rating > 5 || suggestion != null && suggestion.length() > 1000) {
            throw ApiException.invalidParameter();
        }
        String cleanedSuggestion = suggestion == null ? null : suggestion.trim();
        return save(ResumeFeedback.review(rating, cleanedSuggestion == null || cleanedSuggestion.isEmpty() ? null : cleanedSuggestion,
                ipAddress, truncateUserAgent(userAgent)));
    }

    @Transactional(readOnly = true)
    public PageResponse<ResumeFeedbackContent> list(int page, int size, ResumeFeedbackType type,
                                                     ResumeFeedbackStatus status, Integer rating) {
        Specification<ResumeFeedback> specification = Specification.unrestricted();
        if (type != null) specification = specification.and((root, query, builder) -> builder.equal(root.get("type"), type));
        if (status != null) specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        if (rating != null) specification = specification.and((root, query, builder) -> builder.equal(root.get("rating"), rating));
        return PageResponse.from(feedbacks.findAll(specification, PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("createdAt").descending())).map(ResumeFeedbackContent::from));
    }

    @Transactional
    public ResumeFeedbackContent updateStatus(String id, ResumeFeedbackStatus status) {
        if (status == null) throw ApiException.invalidParameter();
        ResumeFeedback feedback = feedbacks.findById(id).orElseThrow(ApiException::notFound);
        feedback.changeStatus(status);
        audit.event("RESUME_FEEDBACK_STATUS_UPDATED", "ResumeFeedback", feedback.getId());
        return ResumeFeedbackContent.from(feedback);
    }

    private ResumeFeedbackContent save(ResumeFeedback feedback) {
        ResumeFeedback saved = feedbacks.save(feedback);
        audit.event("RESUME_FEEDBACK_RECEIVED", "ResumeFeedback", saved.getId());
        return ResumeFeedbackContent.from(saved);
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) return null;
        return userAgent.substring(0, Math.min(userAgent.length(), 512));
    }
}
