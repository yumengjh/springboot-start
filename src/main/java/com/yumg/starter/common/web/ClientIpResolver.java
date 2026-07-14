package com.yumg.starter.common.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

/** Resolves forwarded client addresses only when the direct sender is trusted. */
public final class ClientIpResolver {
    private final List<String> trustedProxies;

    public ClientIpResolver(String trustedProxies) {
        this.trustedProxies = Arrays.stream(trustedProxies.split(",")).map(String::trim)
                .filter(value -> !value.isEmpty()).toList();
    }

    public String resolve(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        if (!trusted(remote)) return remote;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) return remote;
        String[] chain = forwarded.split(",");
        for (int index = chain.length - 1; index >= 0; index--) {
            String candidate = chain[index].trim();
            if (!candidate.isEmpty() && !trusted(candidate)) return candidate;
        }
        return remote;
    }

    private boolean trusted(String address) {
        return trustedProxies.stream().anyMatch(network -> matches(network, address));
    }

    private boolean matches(String network, String address) {
        try {
            String[] parts = network.split("/", 2);
            byte[] base = InetAddress.getByName(parts[0]).getAddress();
            byte[] candidate = InetAddress.getByName(address).getAddress();
            if (base.length != candidate.length) return false;
            int prefix = parts.length == 1 ? base.length * 8 : Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > base.length * 8) return false;
            for (int index = 0; index < prefix / 8; index++) if (base[index] != candidate[index]) return false;
            if (prefix % 8 == 0) return true;
            int mask = 0xFF << (8 - prefix % 8);
            return (base[prefix / 8] & mask) == (candidate[prefix / 8] & mask);
        } catch (Exception ignored) {
            return false;
        }
    }
}
