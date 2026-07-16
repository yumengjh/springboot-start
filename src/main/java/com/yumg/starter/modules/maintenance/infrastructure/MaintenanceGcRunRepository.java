package com.yumg.starter.modules.maintenance.infrastructure;
import com.yumg.starter.entities.MaintenanceGcRun;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MaintenanceGcRunRepository extends JpaRepository<MaintenanceGcRun, String> {}
