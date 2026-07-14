package com.yumg.starter.modules.announcements.api.dto;

/** Public representation of one already-published announcement. */
public record AnnouncementPublicContentResponse(
        String title,
        String content,
        String authorDisplayName,
        long version) {
}
