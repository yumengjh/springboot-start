package com.yumg.starter.modules.security.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yumg.starter.entities.IpAccessRule;
import com.yumg.starter.modules.security.infrastructure.IpAccessRuleRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IpAccessRuleServiceTest {
    @Test
    void decidesFromOneRefreshedSnapshotWithoutQueryingOnEveryRequest() {
        IpAccessRuleRepository repository = Mockito.mock(IpAccessRuleRepository.class);
        when(repository.findAllByScope("API")).thenReturn(List.of(
                new IpAccessRule(IpAccessRule.Type.ALLOW, "10.0.0.0/8", "API", null, "office"),
                new IpAccessRule(IpAccessRule.Type.DENY, "10.1.0.0/16", "API", null, "blocked subnet")));
        IpAccessRuleService service = new IpAccessRuleService(repository, Mockito.mock(AuditService.class));

        service.refreshSnapshot();

        assertThat(service.decision("10.0.4.8", Instant.now())).isEqualTo(IpAccessRuleService.Decision.ALLOW);
        assertThat(service.decision("10.1.4.8", Instant.now())).isEqualTo(IpAccessRuleService.Decision.DENY);
        assertThat(service.decision("192.168.1.4", Instant.now())).isEqualTo(IpAccessRuleService.Decision.DENY);
        verify(repository, times(1)).findAllByScope("API");
    }
}
