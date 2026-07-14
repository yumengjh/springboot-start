package com.yumg.starter.common.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an MVC endpoint as accessible without an access token. The request still
 * passes through the trace, IP access, endpoint policy and rate-limit filters.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicApi {
    /** Whether the endpoint returns its body directly instead of the standard API envelope. */
    boolean minimalResponse() default false;

    /** Authority that upgrades a public caller to the standard detailed response. */
    String detailedAuthority() default "";
}
