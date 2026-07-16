package com.yumg.starter.entities;

import com.yumg.starter.common.entity.InstantStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "maintenance_gc_locks")
public class MaintenanceGcLock {
    @Id @Column(name = "lock_name", length = 100) private String lockName;
    @Column(name = "run_id", length = 36) private String runId;
    @Column(name = "locked_until") @Convert(converter = InstantStringConverter.class) private Instant lockedUntil;
    @Column(name = "updated_at", nullable = false) @Convert(converter = InstantStringConverter.class) private Instant updatedAt;
    protected MaintenanceGcLock() {}
    public MaintenanceGcLock(String lockName) { this.lockName = lockName; this.updatedAt = Instant.now(); }
}
