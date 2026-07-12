package com.yumg.starter.modules.users.api.dto;

import com.yumg.starter.entities.User;

public record AdminUserResponse(String id, String username, String displayName, String status) {
    public static AdminUserResponse from(User user) { return new AdminUserResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getStatus().name()); }
}
