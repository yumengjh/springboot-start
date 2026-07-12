package com.yumg.starter.entities;

import com.yumg.starter.common.entity.BaseUuidEntity;
import com.yumg.starter.common.entity.InstantStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "refresh_sessions")
public class RefreshSession extends BaseUuidEntity {
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    @Column(name = "issued_at", nullable = false)
    @Convert(converter = InstantStringConverter.class)
    private Instant issuedAt;
    @Column(name = "expires_at", nullable = false)
    @Convert(converter = InstantStringConverter.class)
    private Instant expiresAt;

    protected RefreshSession() {}

    public RefreshSession(String userId, String familyId, String tokenHash, Instant issuedAt,
                          Instant expiresAt) {
        this.userId = userId;
        this.familyId = familyId;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}
