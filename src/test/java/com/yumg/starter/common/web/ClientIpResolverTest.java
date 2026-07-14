package com.yumg.starter.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTest {

    @Test
    void ignoresForwardedHeaderFromAnUntrustedSender() {
        ClientIpResolver resolver = new ClientIpResolver("10.0.0.0/8");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "198.51.100.3");

        assertThat(resolver.resolve(request)).isEqualTo("203.0.113.10");
    }

    @Test
    void selectsFirstNonTrustedAddressFromTrustedProxyChain() {
        ClientIpResolver resolver = new ClientIpResolver("10.0.0.0/8,127.0.0.1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");
        request.addHeader("X-Forwarded-For", "198.51.100.3, 10.0.0.9");

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.3");
    }
}
