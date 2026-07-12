package com.yumg.starter.common.api;

import com.yumg.starter.common.web.TraceIdFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorWriter {
    public void write(HttpServletResponse response, ApiErrorCode code) throws IOException {
        ApiError error = ApiErrorFactory.create(code, List.of());
        response.setStatus(code.status().value());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(TraceIdFilter.TRACE_ID_HEADER, error.traceId());
        response.getWriter().write("{\"code\":\"%s\",\"message\":\"%s\",\"traceId\":\"%s\",\"violations\":[],\"timestamp\":\"%s\"}"
                .formatted(error.code(), error.message(), error.traceId(), error.timestamp()));
    }
}
