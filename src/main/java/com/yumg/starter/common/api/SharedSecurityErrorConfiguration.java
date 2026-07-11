package com.yumg.starter.common.api;

import com.yumg.starter.common.web.TraceIdFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration(proxyBeanMethods = false)
public class SharedSecurityErrorConfiguration {
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain sharedSecurityFilterChain(HttpSecurity http, SecurityApiErrorHandler errors,
                                                  TraceIdFilter traceIdFilter)
            throws Exception {
        return http
            .addFilterBefore(traceIdFilter, SecurityContextHolderFilter.class)
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(errors)
                .accessDeniedHandler(errors))
            .build();
    }
}
