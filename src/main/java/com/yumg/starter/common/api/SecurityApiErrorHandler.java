package com.yumg.starter.common.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public final class SecurityApiErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException, ServletException {
        write(response, ApiErrorCode.AUTHENTICATION_REQUIRED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException, ServletException {
        write(response, ApiErrorCode.ACCESS_DENIED);
    }

    private void write(HttpServletResponse response, ApiErrorCode code) throws IOException {
        ApiError error = ApiErrorFactory.create(code, List.of());
        response.setStatus(code.status().value());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(com.yumg.starter.common.web.TraceIdFilter.TRACE_ID_HEADER, error.traceId());
        response.getWriter().write("{\"code\":\"%s\",\"message\":\"%s\",\"traceId\":\"%s\",\"violations\":[],\"timestamp\":\"%s\"}"
            .formatted(error.code(), error.message(), error.traceId(), error.timestamp()));
    }
}
