package com.yumg.starter.modules.rbac.infrastructure;

import com.yumg.starter.entities.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> { Optional<Role> findByCode(String code); }
