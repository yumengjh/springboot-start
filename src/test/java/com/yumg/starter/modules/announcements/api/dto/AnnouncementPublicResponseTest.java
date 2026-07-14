package com.yumg.starter.modules.announcements.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AnnouncementPublicResponseTest {

    @Test
    void listResponseExposesOnlyTheIdentifierNeededToLoadContent() {
        var response = new AnnouncementPublicListResponse("announcement-1", "Title", "Alice", 7L);

        assertThat(response.id()).isEqualTo("announcement-1");
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.authorDisplayName()).isEqualTo("Alice");
        assertThat(response.version()).isEqualTo(7L);
    }

    @Test
    void contentResponseIncludesOnlyPublishedContentFields() {
        var response = new AnnouncementPublicContentResponse("Title", "Content", "Alice", 7L);

        assertThat(response.content()).isEqualTo("Content");
        assertThat(response.authorDisplayName()).isEqualTo("Alice");
    }
}
