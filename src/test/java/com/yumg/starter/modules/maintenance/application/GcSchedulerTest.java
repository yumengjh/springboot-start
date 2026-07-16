package com.yumg.starter.modules.maintenance.application;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void recordsFailuresAndClearsThemAfterTheNextSuccessfulScheduledRun() {
        GcExecutionService execution = Mockito.mock(GcExecutionService.class);
        RuntimeSettingService settings = Mockito.mock(RuntimeSettingService.class);
        when(settings.enabled("maintenance.gc.enabled")).thenReturn(true);
        when(settings.enabled("maintenance.gc.schedule.enabled")).thenReturn(true);
        when(settings.integer("maintenance.gc.interval-minutes")).thenReturn(0);
        when(execution.run("SCHEDULED", false, java.util.List.of(), null))
                .thenThrow(new IllegalStateException("database unavailable")).thenReturn(null);
        GcScheduler scheduler = new GcScheduler(execution, settings);

        scheduler.runWhenDue();
        assertThat(scheduler.status().consecutiveFailures()).isEqualTo(1);
        assertThat(scheduler.status().lastFailureMessage()).contains("database unavailable");

        scheduler.runWhenDue();
        assertThat(scheduler.status().consecutiveFailures()).isZero();
        assertThat(scheduler.status().lastSucceededAt()).isNotNull();
    }
}
