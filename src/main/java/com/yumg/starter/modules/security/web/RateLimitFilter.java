package com.yumg.starter.modules.security.web;

import com.yumg.starter.common.api.ApiErrorCode;
import com.yumg.starter.common.api.ApiErrorWriter;
import com.yumg.starter.modules.security.application.RateLimitService;
import com.yumg.starter.modules.security.application.EndpointRateLimitPolicy;
import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import com.yumg.starter.common.web.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService limits;
    private final RuntimeSettingService settings;
    private final ApiErrorWriter errors;
    private final ClientIpResolver clientIp;

    public RateLimitFilter(RateLimitService limits, RuntimeSettingService settings, ApiErrorWriter errors, ClientIpResolver clientIp) {
        this.limits = limits;
        this.settings = settings;
        this.errors = errors;
        this.clientIp = clientIp;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/")
                || HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = clientIp.resolve(request) + ':' + request.getRequestURI();
        String path = request.getRequestURI();
        EndpointRateLimitPolicy endpointPolicy = new EndpointRateLimitPolicy(settings.string("security.endpoint.rate-limit.patterns"));
        boolean allowed = endpointPolicy.matches(path)
                ? limits.allow("endpoint:" + key, settings.integer("security.endpoint.rate-limit.capacity"), settings.integer("security.endpoint.rate-limit.window-seconds"))
                : limits.allow(key);
        if (!allowed) {
            errors.write(response, ApiErrorCode.RATE_LIMITED);
            return;
        }
        chain.doFilter(request, response);
    }
}
