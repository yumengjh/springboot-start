package com.yumg.starter.modules.maintenance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yumg.starter.entities.MaintenanceGcPolicy;
import com.yumg.starter.modules.security.application.RateLimitService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RateLimitStateGcResourceTest {
    @Test
    void previewCountsOnlyExpiredWindowsThatRealCleanupCanDelete() {
        RateLimitService limits = Mockito.mock(RateLimitService.class);
        when(limits.expiredStateCount(any(Instant.class))).thenReturn(2L);
        RateLimitStateGcResource resource = new RateLimitStateGcResource(limits);

        GcResourceResult result = resource.execute(new MaintenanceGcPolicy("rate-limit-state", true, true, 0, 1000), true, Instant.now());

        assertThat(result.candidates()).isEqualTo(2);
        assertThat(result.deleted()).isZero();
        verify(limits, never()).cleanupExpired(any(Instant.class));
    }
}
