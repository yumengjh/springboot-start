package com.yumg.starter.modules.maintenance.infrastructure;
import com.yumg.starter.entities.MaintenanceGcLock;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface MaintenanceGcLockRepository extends JpaRepository<MaintenanceGcLock, String> {
    @Modifying
    @Query("update MaintenanceGcLock l set l.runId = :runId, l.lockedUntil = :until, l.updatedAt = :now where l.lockName = :name and (l.lockedUntil is null or l.lockedUntil < :now)")
    int tryAcquire(@Param("name") String name, @Param("runId") String runId, @Param("now") Instant now, @Param("until") Instant until);
    @Modifying
    @Query("update MaintenanceGcLock l set l.runId = null, l.lockedUntil = null, l.updatedAt = :now where l.lockName = :name and l.runId = :runId")
    int release(@Param("name") String name, @Param("runId") String runId, @Param("now") Instant now);
}
