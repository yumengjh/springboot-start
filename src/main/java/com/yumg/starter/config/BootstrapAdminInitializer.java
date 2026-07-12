package com.yumg.starter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
public class BootstrapAdminInitializer {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    CommandLineRunner bootstrapAdmin(BootstrapAdminService bootstrapAdminService,
            @Value("${APP_BOOTSTRAP_ADMIN_USERNAME:}") String username,
            @Value("${APP_BOOTSTRAP_ADMIN_PASSWORD:}") String password,
            @Value("${APP_BOOTSTRAP_ADMIN_RESET_PASSWORD:false}") boolean resetPassword) {
        return args -> bootstrapAdminService.initialize(username, password, resetPassword);
    }
}
