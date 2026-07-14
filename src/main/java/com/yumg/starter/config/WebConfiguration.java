package com.yumg.starter.config;

import com.yumg.starter.common.web.ClientIpResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WebConfiguration {
    @Bean ClientIpResolver clientIpResolver(@Value("${app.security.trusted-proxies:}") String trustedProxies) {
        return new ClientIpResolver(trustedProxies);
    }
}
