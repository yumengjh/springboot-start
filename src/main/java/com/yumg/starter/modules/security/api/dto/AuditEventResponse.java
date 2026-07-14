package com.yumg.starter.modules.security.api.dto;

import com.yumg.starter.entities.AuditEvent;

public record AuditEventResponse(String id, String actorId, String action, String result, String targetType,
                                 String targetId, long occurredAt, String traceId, String metadata) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getActorId(), event.getAction(), event.getResult(), event.getTargetType(),
                event.getTargetId(), event.getOccurredAt().toEpochMilli(), event.getTraceId(), event.getMetadata());
    }
}
