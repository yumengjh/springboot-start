package com.yumg.starter.modules.security.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EndpointRateLimitPolicyTest {

    @Test
    void matchesOnlyConfiguredEndpointPatterns() {
        EndpointRateLimitPolicy policy = new EndpointRateLimitPolicy("/api/v1/auth/login,/api/v1/public/**");

        assertThat(policy.matches("/api/v1/auth/login")).isTrue();
        assertThat(policy.matches("/api/v1/public/news")).isTrue();
        assertThat(policy.matches("/api/v1/admin/users")).isFalse();
    }
}
