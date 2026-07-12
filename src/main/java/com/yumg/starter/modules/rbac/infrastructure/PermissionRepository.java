package com.yumg.starter.modules.rbac.infrastructure;

import com.yumg.starter.entities.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> { Optional<Permission> findByCode(String code); }
