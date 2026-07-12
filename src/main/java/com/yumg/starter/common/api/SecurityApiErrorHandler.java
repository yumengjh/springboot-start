package com.yumg.starter.common.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public final class SecurityApiErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ApiErrorWriter errors;

    public SecurityApiErrorHandler(ApiErrorWriter errors) {
        this.errors = errors;
    }
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException, ServletException {
        errors.write(response, ApiErrorCode.AUTHENTICATION_REQUIRED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException, ServletException {
        errors.write(response, ApiErrorCode.ACCESS_DENIED);
    }
}
