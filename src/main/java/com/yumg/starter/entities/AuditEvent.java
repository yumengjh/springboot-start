package com.yumg.starter.entities;

import com.yumg.starter.common.entity.BaseUuidEntity;
import com.yumg.starter.common.entity.InstantStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent extends BaseUuidEntity {
    @Column(name = "actor_id", length = 36) private String actorId;
    @Column(nullable = false, length = 160) private String action;
    @Column(nullable = false, length = 32) private String result;
    @Column(name = "target_type", length = 100) private String targetType;
    @Column(name = "target_id", length = 100) private String targetId;
    @Column(name = "occurred_at", nullable = false) @Convert(converter = InstantStringConverter.class) private Instant occurredAt;
    @Column(name = "trace_id", length = 100) private String traceId;
    @Column(name = "metadata_json", columnDefinition = "text") private String metadata;
    protected AuditEvent() {}
    public AuditEvent(String actorId, String action, String targetType, String targetId, String result,
                      String traceId, String metadata) {
        this.actorId=actorId; this.action=action; this.result=result; this.targetType=targetType;
        this.targetId=targetId; this.traceId=traceId; this.metadata=metadata; this.occurredAt=Instant.now();
    }
    public String getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getResult() { return result; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getTraceId() { return traceId; }
    public String getMetadata() { return metadata; }
}
