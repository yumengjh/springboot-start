package com.yumg.starter.entities;

import com.yumg.starter.common.entity.BaseUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent extends BaseUuidEntity {
    @Column(nullable = false, length = 160) private String action;
    @Column(nullable = false, length = 32) private String result;
    @Column(name = "target_type", length = 100) private String targetType;
    @Column(name = "target_id", length = 100) private String targetId;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    @Column(name = "trace_id", length = 100) private String traceId;
    @Column(name = "metadata_json", columnDefinition = "text") private String metadata;
    protected AuditEvent() {}
    public AuditEvent(String action, String targetType, String targetId, String traceId, String metadata) { this.action=action; this.result="SUCCESS"; this.targetType=targetType; this.targetId=targetId; this.traceId=traceId; this.metadata=metadata; this.occurredAt=Instant.now(); }
}
