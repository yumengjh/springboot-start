package com.yumg.starter.modules.auth.infrastructure;

import com.yumg.starter.entities.User;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    long countByRoles_Code(String roleCode);
    List<User> findDistinctByRoles_Code(String roleCode);
}
