package com.yumg.starter.modules.resume.infrastructure;

import com.yumg.starter.entities.ResumeDocument;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeDocumentRepository extends JpaRepository<ResumeDocument, String> {

    Optional<ResumeDocument> findFirstByOrderByCreatedAtAsc();
}
