package com.yumg.starter.modules.runtimeconfig.infrastructure;

import com.yumg.starter.entities.RuntimeSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuntimeSettingRepository extends JpaRepository<RuntimeSetting, String> {
    Optional<RuntimeSetting> findByKey(String key);
}
