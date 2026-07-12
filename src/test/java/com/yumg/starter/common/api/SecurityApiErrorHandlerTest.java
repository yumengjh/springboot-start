package com.yumg.starter.common.api;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yumg.starter.common.web.TraceIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitWebConfig(SecurityApiErrorHandlerTest.Config.class)
@TestPropertySource(properties = "app.security.shared-fallback=true")
class SecurityApiErrorHandlerTest {
    @Autowired WebApplicationContext context;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void unauthenticatedRequestUsesApiErrorEnvelope() throws Exception {
        mvc.perform(get("/admin").header("X-Trace-Id", "security_trace"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("X-Trace-Id", "security_trace"))
            .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
            .andExpect(jsonPath("$.traceId").value("security_trace"));
    }

    @Test
    void accessDeniedHandlerUsesApiErrorEnvelope() throws Exception {
        var response = new MockHttpServletResponse();
        context.getBean(SecurityApiErrorHandler.class).handle(
            new MockHttpServletRequest(), response, new AccessDeniedException("secret"));

        org.assertj.core.api.Assertions.assertThat(response.getStatus()).isEqualTo(403);
        org.assertj.core.api.Assertions.assertThat(response.getContentAsString())
            .contains("\"code\":\"ACCESS_DENIED\"")
            .doesNotContain("secret");
    }

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    @Import(SharedSecurityErrorConfiguration.class)
    static class Config {
        @Bean TraceIdFilter traceIdFilter() { return new TraceIdFilter(); }
        @Bean ApiErrorWriter apiErrorWriter() { return new ApiErrorWriter(); }
        @Bean SecurityApiErrorHandler securityApiErrorHandler(ApiErrorWriter errors) { return new SecurityApiErrorHandler(errors); }
        @Bean TestController testController() { return new TestController(); }
    }

    @RestController
    static class TestController {
        @GetMapping("/admin") String admin() { return "ok"; }
    }
}
