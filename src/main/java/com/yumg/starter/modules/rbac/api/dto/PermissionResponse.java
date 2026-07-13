package com.yumg.starter.modules.rbac.api.dto;

import com.yumg.starter.entities.Permission;

public record PermissionResponse(String code) {
    public static PermissionResponse from(Permission permission) { return new PermissionResponse(permission.getCode()); }
}
