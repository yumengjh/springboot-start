package com.yumg.starter.modules.announcements.api.dto;
import com.yumg.starter.entities.Announcement;
import java.time.Instant;
public record AnnouncementResponse(String id, String title, String content, boolean published, Instant publishedAt, String authorId, long version) {
    public static AnnouncementResponse from(Announcement item) { return new AnnouncementResponse(item.getId(), item.getTitle(), item.getContent(), item.isPublished(), item.getPublishedAt(), item.getAuthorId(), item.getVersion()); }
}
