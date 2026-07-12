package com.yumg.starter.modules.users.api.dto;

import com.yumg.starter.entities.User;

public record CurrentUserResponse(String id, String username, String displayName, String status) {
    public static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getDisplayName(),
                user.getStatus().name());
    }
}
