package com.yumg.starter.entities;

import com.yumg.starter.common.entity.AuditedEntity;
import com.yumg.starter.common.entity.InstantStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "announcements")
public class Announcement extends AuditedEntity {
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, columnDefinition = "text") private String content;
    @Column(nullable = false) private boolean published;
    @Column(name = "published_at") @Convert(converter = InstantStringConverter.class) private Instant publishedAt;
    @Column(name = "author_id", nullable = false, length = 36) private String authorId;
    protected Announcement() { }
    public Announcement(String title, String content, String authorId) { this.title = title; this.content = content; this.authorId = authorId; }
    public void update(String title, String content) { this.title = title; this.content = content; }
    public void publish() { published = true; publishedAt = Instant.now(); }
    public void unpublish() { published = false; publishedAt = null; }
    public String getTitle() { return title; } public String getContent() { return content; } public boolean isPublished() { return published; }
    public Instant getPublishedAt() { return publishedAt; } public String getAuthorId() { return authorId; }
}
