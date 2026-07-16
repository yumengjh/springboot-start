package com.yumg.starter.modules.security.infrastructure;
import com.yumg.starter.entities.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
    Page<AuditEvent> findAllByOrderByOccurredAtDesc(Pageable pageable);
    long countByOccurredAtBefore(java.time.Instant cutoff);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from AuditEvent e where e.occurredAt < :cutoff")
    int deleteByOccurredAtBefore(@org.springframework.data.repository.query.Param("cutoff") java.time.Instant cutoff);
}
