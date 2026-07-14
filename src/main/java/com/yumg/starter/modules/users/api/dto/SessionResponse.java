package com.yumg.starter.modules.users.api.dto;

import com.yumg.starter.entities.RefreshSession;

public record SessionResponse(String id, long issuedAt, long expiresAt, Long revokedAt) {
    public static SessionResponse from(RefreshSession session) { return new SessionResponse(session.getId(), session.getIssuedAt().toEpochMilli(), session.getExpiresAt().toEpochMilli(), session.getRevokedAt() == null ? null : session.getRevokedAt().toEpochMilli()); }
}
