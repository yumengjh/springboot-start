package com.yumg.starter.modules.maintenance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yumg.starter.entities.MaintenanceGcPolicy;
import com.yumg.starter.entities.MaintenanceGcRun;
import com.yumg.starter.modules.maintenance.infrastructure.MaintenanceGcLockRepository;
import com.yumg.starter.modules.maintenance.infrastructure.MaintenanceGcRunRepository;
import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import com.yumg.starter.modules.security.application.AuditService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GcExecutionServiceTest {
    @Test
    void dryRunPreviewsSelectedResourceWithoutMutatingIt() {
        GcPolicyService policies = Mockito.mock(GcPolicyService.class);
        MaintenanceGcRunRepository runs = Mockito.mock(MaintenanceGcRunRepository.class);
        MaintenanceGcLockRepository locks = Mockito.mock(MaintenanceGcLockRepository.class);
        RuntimeSettingService settings = Mockito.mock(RuntimeSettingService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        GcResource resource = Mockito.mock(GcResource.class);
        MaintenanceGcPolicy policy = new MaintenanceGcPolicy("example", true, true, 30, 100);
        when(resource.descriptor()).thenReturn(new GcResourceDescriptor("example", "示例", "说明", 30, 100, true));
        when(policies.resources()).thenReturn(Map.of("example", resource));
        when(policies.policy("example")).thenReturn(policy);
        when(settings.enabled("maintenance.gc.enabled")).thenReturn(true);
        when(settings.integer("maintenance.gc.max-run-seconds")).thenReturn(120);
        when(locks.tryAcquire(eq("global"), any(), any(), any())).thenReturn(1);
        when(runs.save(any(MaintenanceGcRun.class))).thenAnswer(call -> call.getArgument(0));
        when(resource.execute(eq(policy), eq(true), any(Instant.class)))
                .thenReturn(new GcResourceResult("example", 3, 0, "预览"));

        GcExecutionService service = new GcExecutionService(policies, runs, locks, settings, audit,
                new GcRunSummaryCodec(new tools.jackson.databind.ObjectMapper()));

        GcRunContent result = service.run("MANUAL", true, List.of("example"), "admin");

        assertThat(result.status()).isEqualTo("SUCCEEDED");
        assertThat(result.summary()).isNotNull();
        assertThat(result.summary().totalCandidates()).isEqualTo(3);
        assertThat(result.summary().totalDeleted()).isZero();
        assertThat(result.summary().resources()).singleElement().satisfies(item -> {
            assertThat(item.resourceCode()).isEqualTo("example");
            assertThat(item.candidates()).isEqualTo(3);
            assertThat(item.deleted()).isZero();
            assertThat(item.message()).isEqualTo("预览");
        });
        verify(resource).execute(eq(policy), eq(true), any(Instant.class));
        verify(locks).release(eq("global"), any(), any());
    }
}
