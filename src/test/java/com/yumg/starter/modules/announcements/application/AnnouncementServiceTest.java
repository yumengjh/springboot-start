package com.yumg.starter.modules.announcements.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yumg.starter.common.api.PageResponse;
import com.yumg.starter.entities.Announcement;
import com.yumg.starter.modules.announcements.api.dto.AnnouncementResponse;
import com.yumg.starter.modules.announcements.infrastructure.AnnouncementRepository;
import com.yumg.starter.modules.security.application.AuditService;
import com.yumg.starter.modules.users.application.UserLookupService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AnnouncementServiceTest {
    @Test
    void managedListUsesPageBoundariesInsteadOfReadingEveryAnnouncement() {
        AnnouncementRepository repository = Mockito.mock(AnnouncementRepository.class);
        when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Announcement("Release", "Notes", "admin")), PageRequest.of(1, 10), 21));
        AnnouncementService service = new AnnouncementService(repository, Mockito.mock(AuditService.class), Mockito.mock(UserLookupService.class));

        PageResponse<AnnouncementResponse> page = service.managedPage(1, 10);

        assertThat(page.items()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(21);
        assertThat(page.page()).isEqualTo(1);
    }
}
