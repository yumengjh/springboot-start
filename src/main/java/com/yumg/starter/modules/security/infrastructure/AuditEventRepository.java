package com.yumg.starter.modules.security.infrastructure;
import com.yumg.starter.entities.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {}
