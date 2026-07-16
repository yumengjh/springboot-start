package com.yumg.starter.modules.maintenance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GcSchedulerHealthIndicatorTest {
    @Test
    void reportsUpBeforeTheSchedulerHasRun() {
        GcScheduler scheduler = Mockito.mock(GcScheduler.class);
        when(scheduler.status()).thenReturn(new GcSchedulerStatus(null, null, null, 0, null, null));

        var health = new GcSchedulerHealthIndicator(scheduler).health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails()).containsEntry("consecutiveFailures", 0)
                .doesNotContainKeys("lastSucceededAt", "lastFailedAt");
    }
}
