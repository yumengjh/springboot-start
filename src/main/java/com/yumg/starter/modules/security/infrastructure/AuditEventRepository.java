package com.yumg.starter.modules.security.infrastructure;
import com.yumg.starter.entities.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
    Page<AuditEvent> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
