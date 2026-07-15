package com.yumg.starter.modules.resume.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.ResumeDocument;
import com.yumg.starter.modules.resume.infrastructure.ResumeDocumentRepository;
import com.yumg.starter.modules.security.application.AuditService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ResumeServiceTest {

    private final ResumeDocumentRepository documents = Mockito.mock(ResumeDocumentRepository.class);
    private final AuditService audit = Mockito.mock(AuditService.class);
    private final ResumeService service = new ResumeService(documents, audit, new ObjectMapper());

    @Test
    void createsDefaultDocumentContainingCurrentSupportedSectionTypes() throws Exception {
        when(documents.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.empty());
        when(documents.save(any(ResumeDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var document = service.createDefaultIfAbsent();
        var root = new ObjectMapper().readTree(document.content());

        assertThat(root.at("/profile/name").asText()).isNotBlank();
        assertThat(root.at("/profile/contacts")).isNotEmpty();
        assertThat(root.at("/sections")).extracting(node -> node.path("type").asText())
                .contains("bullet-list", "timeline", "projects");
    }

    @Test
    void rejectsContentWithoutAProfileNameAndSections() {
        when(documents.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(validDocument()));

        assertThatThrownBy(() -> service.update("{\"profile\":{}}", 1, 0))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsUnsupportedSectionTypeWithoutCustomPayload() {
        when(documents.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(validDocument()));

        assertThatThrownBy(() -> service.update("""
                {"profile":{"name":"Alice","contacts":[]},"sections":[
                  {"id":"extra","type":"unknown","title":"Extra","items":[]}
                ]}
                """, 1, 0)).isInstanceOf(ApiException.class);
    }

    @Test
    void updatesDocumentAndWritesAuditEvent() {
        ResumeDocument document = validDocument();
        when(documents.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(document));

        var result = service.update(validContent(), 1, 0);

        assertThat(result.content()).isEqualTo(validContent());
        verify(audit).event("RESUME_UPDATED", "ResumeDocument", document.getId());
    }

    @Test
    void rejectsStaleDocumentVersion() {
        ResumeDocument document = validDocument();
        org.springframework.test.util.ReflectionTestUtils.setField(document, "version", 4L);
        when(documents.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> service.update(validContent(), 1, 3)).isInstanceOf(ApiException.class);
    }

    private ResumeDocument validDocument() {
        return new ResumeDocument(validContent(), 1);
    }

    private String validContent() {
        return """
                {"profile":{"name":"Alice","contacts":[]},"sections":[
                  {"id":"summary","type":"bullet-list","title":"简介","items":["Hello"]}
                ]}
                """;
    }
}
