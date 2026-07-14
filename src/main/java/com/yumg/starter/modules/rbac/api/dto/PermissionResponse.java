package com.yumg.starter.modules.rbac.api.dto;

import com.yumg.starter.entities.Permission;

public record PermissionResponse(String code, String description) {
    public static PermissionResponse from(Permission permission) { return new PermissionResponse(permission.getCode(), permission.getDescription()); }
}
