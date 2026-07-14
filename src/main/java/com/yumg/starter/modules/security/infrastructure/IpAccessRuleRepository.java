package com.yumg.starter.modules.security.infrastructure;

import com.yumg.starter.entities.IpAccessRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IpAccessRuleRepository extends JpaRepository<IpAccessRule, String> {
    List<IpAccessRule> findAllByScope(String scope);
}
