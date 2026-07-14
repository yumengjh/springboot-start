package com.yumg.starter.modules.announcements.api.dto;

/** Compact public representation used for an announcement index. */
public record AnnouncementPublicListResponse(
        String id,
        String title,
        String authorDisplayName,
        long version) {
}
