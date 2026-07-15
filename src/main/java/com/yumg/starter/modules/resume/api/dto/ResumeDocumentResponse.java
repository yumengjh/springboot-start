package com.yumg.starter.modules.resume.api.dto;

import com.yumg.starter.modules.resume.application.ResumeDocumentContent;

public record ResumeDocumentResponse(String content, int schemaVersion, long version) {

    public static ResumeDocumentResponse from(ResumeDocumentContent document) {
        return new ResumeDocumentResponse(document.content(), document.schemaVersion(), document.version());
    }
}
