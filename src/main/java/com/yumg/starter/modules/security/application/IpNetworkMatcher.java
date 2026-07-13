package com.yumg.starter.modules.security.application;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** Stateless exact-address and CIDR matcher for runtime access policies. */
public final class IpNetworkMatcher {
    private IpNetworkMatcher() { }

    public static boolean matches(String network, String address) {
        try {
            String[] parts = network.trim().split("/", 2);
            byte[] base = InetAddress.getByName(parts[0]).getAddress();
            byte[] candidate = InetAddress.getByName(address).getAddress();
            if (base.length != candidate.length) return false;
            int prefix = parts.length == 1 ? base.length * 8 : Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > base.length * 8) return false;
            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;
            for (int index = 0; index < fullBytes; index++) if (base[index] != candidate[index]) return false;
            if (remainingBits == 0) return true;
            int mask = 0xFF << (8 - remainingBits);
            return (base[fullBytes] & mask) == (candidate[fullBytes] & mask);
        } catch (UnknownHostException | NumberFormatException ignored) {
            return false;
        }
    }
}
