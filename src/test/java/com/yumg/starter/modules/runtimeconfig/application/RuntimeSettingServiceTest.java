package com.yumg.starter.modules.runtimeconfig.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.yumg.starter.entities.RuntimeSetting;
import com.yumg.starter.modules.runtimeconfig.infrastructure.RuntimeSettingRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RuntimeSettingServiceTest {
    @Test
    void returnsReadableMetadataForBuiltInRuntimeSettings() {
        RuntimeSettingRepository repository = Mockito.mock(RuntimeSettingRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                new RuntimeSetting("identity.registration.enabled", "BOOLEAN", "true")));

        var response = new RuntimeSettingService(repository).list().getFirst();

        assertThat(response.displayName()).isEqualTo("开放自助注册");
        assertThat(response.description()).isEqualTo("控制访客是否可以自行注册新账号。");
    }
}
