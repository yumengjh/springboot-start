package com.yumg.starter.modules.resume.api;

import com.yumg.starter.common.web.PublicApi;
import com.yumg.starter.modules.resume.api.dto.ResumeDocumentRequest;
import com.yumg.starter.modules.resume.api.dto.ResumeDocumentResponse;
import com.yumg.starter.modules.resume.application.ResumeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resume")
@Tag(name = "简历管理")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resume;

    public ResumeController(ResumeService resume) {
        this.resume = resume;
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
}
