package com.yumg.starter.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SecurityConfigurationCorsTest {

    @Test
    void allowsPreflightForEveryMutatingApiMethodUsedByTheAdminConsole() {
        var source = new SecurityConfiguration().corsConfigurationSource();
        var request = new MockHttpServletRequest("OPTIONS", "/api/v1/admin/users/user-id/status");

        var configuration = source.getCorsConfiguration(request);

        assertThat(configuration.getAllowedMethods())
                .containsExactlyInAnyOrderElementsOf(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    }
}
