package com.yumg.starter.modules.maintenance.infrastructure;
import com.yumg.starter.entities.MaintenanceGcPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MaintenanceGcPolicyRepository extends JpaRepository<MaintenanceGcPolicy, String> {
    Optional<MaintenanceGcPolicy> findByResourceCode(String resourceCode);
}
