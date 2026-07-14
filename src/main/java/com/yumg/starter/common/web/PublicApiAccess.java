package com.yumg.starter.common.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Shared authority check for public endpoints that can return a richer authenticated view. */
public final class PublicApiAccess {
    private PublicApiAccess() {
    }

    public static boolean hasAuthority(String authority) {
        if (authority == null || authority.isBlank()) return false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }
}
