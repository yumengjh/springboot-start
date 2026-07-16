package com.yumg.starter.modules.resume.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.ResumeFeedback;
import com.yumg.starter.modules.resume.infrastructure.ResumeFeedbackRepository;
import com.yumg.starter.modules.security.application.AuditService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ResumeFeedbackServiceTest {

    private final ResumeFeedbackRepository feedbacks = Mockito.mock(ResumeFeedbackRepository.class);
    private final AuditService audit = Mockito.mock(AuditService.class);
    private final ResumeFeedbackService service = new ResumeFeedbackService(feedbacks, audit);

    @Test
    void submitsAReactionWithAnonymousRequestMetadata() {
        when(feedbacks.save(any(ResumeFeedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.submitReaction(ResumeReaction.GREAT, "203.0.113.9", "test-agent");

        assertThat(saved.type()).isEqualTo(ResumeFeedbackType.REACTION);
        assertThat(saved.reaction()).isEqualTo(ResumeReaction.GREAT);
        assertThat(saved.ipAddress()).isEqualTo("203.0.113.9");
        verify(audit).event("RESUME_FEEDBACK_RECEIVED", "ResumeFeedback", saved.id());
    }

    @Test
    void submitsAReviewAndNormalizesOptionalSuggestion() {
        when(feedbacks.save(any(ResumeFeedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.submitReview(4, "  内容结构很清晰  ", "203.0.113.10", "test-agent");

        assertThat(saved.type()).isEqualTo(ResumeFeedbackType.REVIEW);
        assertThat(saved.rating()).isEqualTo(4);
        assertThat(saved.suggestion()).isEqualTo("内容结构很清晰");
    }

    @Test
    void rejectsInvalidReviewsButAllowsRepeatedFeedback() {
        assertThatThrownBy(() -> service.submitReview(0, "", "203.0.113.9", "agent"))
                .isInstanceOf(ApiException.class);
        when(feedbacks.save(any(ResumeFeedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.submitReaction(ResumeReaction.GOOD, "203.0.113.9", "agent").reaction())
                .isEqualTo(ResumeReaction.GOOD);
    }
}
