package com.yumg.starter.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseUuidEntity {

    @Id
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Version
    private long version;

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }
}
