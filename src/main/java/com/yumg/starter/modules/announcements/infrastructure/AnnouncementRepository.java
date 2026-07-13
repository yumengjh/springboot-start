package com.yumg.starter.modules.announcements.infrastructure;
import com.yumg.starter.entities.Announcement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AnnouncementRepository extends JpaRepository<Announcement, String> {
    List<Announcement> findByPublishedTrueOrderByPublishedAtDesc();
}
