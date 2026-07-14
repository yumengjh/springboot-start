package com.yumg.starter.modules.security.api.dto;

import com.yumg.starter.entities.IpAccessRule;

public record IpAccessRuleResponse(String id, String type, String network, String scope, Long expiresAt, String reason) {
    public static IpAccessRuleResponse from(IpAccessRule rule) { return new IpAccessRuleResponse(rule.getId(), rule.getType().name(), rule.getNetwork(), rule.getScope(), rule.getExpiresAt()==null?null:rule.getExpiresAt().toEpochMilli(), rule.getReason()); }
}
