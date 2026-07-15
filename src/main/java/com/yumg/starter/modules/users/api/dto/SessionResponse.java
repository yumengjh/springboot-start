package com.yumg.starter.modules.users.api.dto;

import com.yumg.starter.entities.RefreshSession;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record SessionResponse(long firstIssuedAt, long lastActiveAt, long expiresAt,
        String status) {

    public static SessionResponse from(List<RefreshSession> family, Instant now) {
        RefreshSession first = family.stream().min(Comparator.comparing(RefreshSession::getIssuedAt))
                .orElseThrow();
        RefreshSession latest = family.stream().max(Comparator.comparing(RefreshSession::getIssuedAt))
                .orElseThrow();
        return new SessionResponse(first.getIssuedAt().toEpochMilli(),
                latest.getIssuedAt().toEpochMilli(), latest.getExpiresAt().toEpochMilli(),
                status(latest, now));
    }

    private static String status(RefreshSession session, Instant now) {
        if (session.isRevoked()) return "REVOKED";
        if (session.isExpired(now)) return "EXPIRED";
        if (session.isConsumed()) return "ROTATED";
        return "ACTIVE";
    }
}
