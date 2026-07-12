package com.yumg.starter.modules.auth.infrastructure;

import com.yumg.starter.entities.RefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, String> {
}
