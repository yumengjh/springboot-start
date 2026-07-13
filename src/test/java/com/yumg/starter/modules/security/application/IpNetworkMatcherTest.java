package com.yumg.starter.modules.security.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IpNetworkMatcherTest {

    @Test
    void supportsExactAddressesAndCidrNetworks() {
        assertThat(IpNetworkMatcher.matches("127.0.0.1", "127.0.0.1")).isTrue();
        assertThat(IpNetworkMatcher.matches("192.168.10.0/24", "192.168.10.88")).isTrue();
        assertThat(IpNetworkMatcher.matches("192.168.10.0/24", "192.168.11.1")).isFalse();
    }
}
