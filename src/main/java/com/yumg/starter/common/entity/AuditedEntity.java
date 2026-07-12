package com.yumg.starter.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;

@MappedSuperclass
public abstract class AuditedEntity extends BaseUuidEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    @Convert(converter = InstantStringConverter.class)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @Convert(converter = InstantStringConverter.class)
    private Instant updatedAt;

    @PrePersist
    void setCreationTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void setUpdateTimestamp() {
        updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
