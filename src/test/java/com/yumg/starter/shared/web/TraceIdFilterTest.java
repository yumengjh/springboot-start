package com.yumg.starter.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @Test
    void validIncomingTraceIdIsPropagatedAndClearedAfterRequest() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "client_trace-123");
        var response = new MockHttpServletResponse();

        FilterChain chain = (ignoredRequest, ignoredResponse) ->
            assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isEqualTo("client_trace-123");

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo("client_trace-123");
        assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isNull();
    }

    @Test
    void invalidIncomingTraceIdIsReplacedWithGeneratedValue() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "bad value!");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {});

        String generated = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
        assertThat(generated).matches("[a-f0-9]{32}").isNotEqualTo("bad value!");
        assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-"})
    void acceptsBoundaryLengthTraceIds(String traceId) throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, traceId);
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {});

        assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo(traceId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-x", "invalid!trace"})
    void rejectsOutOfRangeOrInvalidTraceIds(String traceId) throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, traceId);
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {});

        assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).matches("[a-f0-9]{32}");
    }

    @Test
    void clearsMdcWhenFilterChainThrows() {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
            () -> filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
                throw new jakarta.servlet.ServletException("boom");
            }));

        assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isNull();
    }
}
