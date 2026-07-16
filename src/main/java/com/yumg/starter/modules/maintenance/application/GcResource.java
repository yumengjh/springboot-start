package com.yumg.starter.modules.maintenance.application;
import com.yumg.starter.entities.MaintenanceGcPolicy;
import java.time.Instant;
public interface GcResource {
    GcResourceDescriptor descriptor();
    GcResourceResult execute(MaintenanceGcPolicy policy, boolean dryRun, Instant now);
}
