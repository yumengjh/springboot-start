package com.yumg.starter.modules.auth.api.dto;

import com.yumg.starter.entities.User;

public record UserResponse(String id, String username, String displayName, String status) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName(),
                user.getStatus().name());
    }
}
