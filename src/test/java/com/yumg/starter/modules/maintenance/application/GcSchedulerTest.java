package com.yumg.starter.modules.maintenance.application;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GcSchedulerTest {
    @Test
    void doesNotRunAutomaticallyWhenScheduledCleanupIsDisabled() {
        GcExecutionService execution = Mockito.mock(GcExecutionService.class);
        RuntimeSettingService settings = Mockito.mock(RuntimeSettingService.class);
        when(settings.enabled("maintenance.gc.enabled")).thenReturn(true);
        when(settings.enabled("maintenance.gc.schedule.enabled")).thenReturn(false);

        new GcScheduler(execution, settings).runWhenDue();

        verify(execution, never()).run(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyList(), Mockito.any());
    }
}
