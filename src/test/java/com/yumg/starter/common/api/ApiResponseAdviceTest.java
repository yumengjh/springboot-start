package com.yumg.starter.common.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yumg.starter.common.web.TraceIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class ApiResponseAdviceTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new ApiResponseAdvice())
            .addFilters(new TraceIdFilter())
            .build();

    @Test
    void wrapsApiSuccessBodyWithDataAndTraceMetadata() throws Exception {
        mvc.perform(get("/api/v1/test").header("X-Trace-Id", "response_trace"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "response_trace"))
                .andExpect(jsonPath("$.data.value").value("ok"))
                .andExpect(jsonPath("$.traceId").value("response_trace"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @RestController
    static class TestController {
        @GetMapping("/api/v1/test") TestResponse test() { return new TestResponse("ok"); }
    }

    record TestResponse(String value) {}
}
