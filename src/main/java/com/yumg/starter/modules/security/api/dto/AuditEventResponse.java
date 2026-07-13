package com.yumg.starter.modules.security.api.dto;

import com.yumg.starter.entities.AuditEvent;
import java.time.Instant;

public record AuditEventResponse(String id, String action, String result, String targetType,
                                 String targetId, Instant occurredAt, String traceId, String metadata) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getAction(), event.getResult(), event.getTargetType(),
                event.getTargetId(), event.getOccurredAt(), event.getTraceId(), event.getMetadata());
    }
}
