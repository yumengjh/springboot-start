package com.yumg.starter.modules.resume.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResumeDocumentRequest(
        @NotBlank @Size(max = 200000) String content,
        @Min(1) int schemaVersion,
        @Min(0) long version) {
}
