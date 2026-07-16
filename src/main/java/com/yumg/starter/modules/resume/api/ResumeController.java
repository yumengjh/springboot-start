package com.yumg.starter.modules.resume.api;

import com.yumg.starter.common.web.PublicApi;
import com.yumg.starter.common.web.ClientIpResolver;
import com.yumg.starter.common.api.PageResponse;
import com.yumg.starter.modules.resume.api.dto.ResumeDocumentRequest;
import com.yumg.starter.modules.resume.api.dto.ResumeDocumentResponse;
import com.yumg.starter.modules.resume.api.dto.ResumeFeedbackResponse;
import com.yumg.starter.modules.resume.api.dto.ResumeFeedbackStatusRequest;
import com.yumg.starter.modules.resume.api.dto.ResumeReactionRequest;
import com.yumg.starter.modules.resume.api.dto.ResumeReviewRequest;
import com.yumg.starter.modules.resume.application.ResumeFeedbackStatus;
import com.yumg.starter.modules.resume.application.ResumeFeedbackType;
import com.yumg.starter.modules.resume.application.ResumeFeedbackService;
import com.yumg.starter.modules.resume.application.ResumeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/resume")
@Tag(name = "简历管理")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resume;
    private final ResumeFeedbackService feedback;
    private final ClientIpResolver clientIp;

    public ResumeController(ResumeService resume, ResumeFeedbackService feedback, ClientIpResolver clientIp) {
        this.resume = resume;
        this.feedback = feedback;
        this.clientIp = clientIp;
    }

    @GetMapping
    @PublicApi
    public ResumeDocumentResponse publicDocument() {
        return ResumeDocumentResponse.from(resume.publicDocument());
    }

    @GetMapping("/manage")
    @PreAuthorize("hasAuthority('resume:manage')")
    public ResumeDocumentResponse managed() {
        return ResumeDocumentResponse.from(resume.managedDocument());
    }

    @PutMapping("/manage")
    @PreAuthorize("hasAuthority('resume:manage')")
    public ResumeDocumentResponse update(@Valid @RequestBody ResumeDocumentRequest request) {
        return ResumeDocumentResponse.from(resume.update(request.content(), request.schemaVersion(), request.version()));
    }

    @PostMapping("/feedback/reactions")
    @PublicApi
    public ResumeFeedbackResponse submitReaction(@Valid @RequestBody ResumeReactionRequest request,
                                                  HttpServletRequest servletRequest) {
        return ResumeFeedbackResponse.from(feedback.submitReaction(request.reaction(), clientIp.resolve(servletRequest),
                servletRequest.getHeader("User-Agent")));
    }

    @PostMapping("/feedback/reviews")
    @PublicApi
    public ResumeFeedbackResponse submitReview(@Valid @RequestBody ResumeReviewRequest request,
                                               HttpServletRequest servletRequest) {
        return ResumeFeedbackResponse.from(feedback.submitReview(request.rating(), request.suggestion(),
                clientIp.resolve(servletRequest), servletRequest.getHeader("User-Agent")));
    }

    @GetMapping("/feedback")
    @PreAuthorize("hasAuthority('resume:manage')")
    public PageResponse<ResumeFeedbackResponse> feedback(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                                                          @RequestParam(required = false) ResumeFeedbackType type,
                                                          @RequestParam(required = false) ResumeFeedbackStatus status,
                                                          @RequestParam(required = false) @Min(1) @Max(5) Integer rating) {
        var source = feedback.list(page, size, type, status, rating);
        return new PageResponse<>(source.items().stream().map(ResumeFeedbackResponse::from).toList(), source.page(),
                source.size(), source.totalElements(), source.totalPages());
    }

    @PatchMapping("/feedback/{id}/status")
    @PreAuthorize("hasAuthority('resume:manage')")
    public ResumeFeedbackResponse updateFeedbackStatus(@PathVariable String id,
                                                        @Valid @RequestBody ResumeFeedbackStatusRequest request) {
        return ResumeFeedbackResponse.from(feedback.updateStatus(id, request.status()));
    }
}
