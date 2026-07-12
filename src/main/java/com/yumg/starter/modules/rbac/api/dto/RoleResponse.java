package com.yumg.starter.modules.rbac.api.dto;

import com.yumg.starter.entities.Role;
import java.util.List;

public record RoleResponse(String code, List<String> permissions) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getCode(), role.getPermissions().stream().map(p -> p.getCode()).sorted().toList());
    }
}
