package com.yumg.starter.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.security.web.util.matcher.RequestMatcher;

/** Resolves the mapped controller method so authorization can honor {@link PublicApi}. */
@Component
public class PublicApiRequestMatcher implements RequestMatcher {
    private final RequestMappingHandlerMapping handlerMappings;

    public PublicApiRequestMatcher(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        try {
            HandlerExecutionChain chain = handlerMappings.getHandler(request);
            if (chain == null || !(chain.getHandler() instanceof HandlerMethod handler)) return false;
            return AnnotatedElementUtils.hasAnnotation(handler.getMethod(), PublicApi.class)
                    || AnnotatedElementUtils.hasAnnotation(handler.getBeanType(), PublicApi.class);
        } catch (Exception ignored) {
            return false;
        }
    }
}
