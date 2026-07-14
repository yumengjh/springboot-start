package com.yumg.starter.modules.announcements.application;
import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.Announcement;
import com.yumg.starter.modules.announcements.api.dto.AnnouncementRequest;
import com.yumg.starter.modules.announcements.api.dto.AnnouncementPublicContentResponse;
import com.yumg.starter.modules.announcements.api.dto.AnnouncementPublicListResponse;
import com.yumg.starter.modules.announcements.api.dto.AnnouncementResponse;
import com.yumg.starter.modules.announcements.infrastructure.AnnouncementRepository;
import com.yumg.starter.modules.security.application.AuditService;
import com.yumg.starter.modules.users.application.UserLookupService;
import java.util.Map;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class AnnouncementService {
 private final AnnouncementRepository announcements; private final AuditService audit; private final UserLookupService users;
 public AnnouncementService(AnnouncementRepository announcements, AuditService audit, UserLookupService users){this.announcements=announcements;this.audit=audit;this.users=users;}
 @Transactional(readOnly=true) public List<AnnouncementPublicListResponse> published(){
  List<Announcement> published=announcements.findByPublishedTrueOrderByPublishedAtDesc();
  Map<String,String> displayNames=users.displayNamesById(published.stream().map(Announcement::getAuthorId).toList());
  return published.stream().map(item -> new AnnouncementPublicListResponse(item.getId(),item.getTitle(),displayNames.get(item.getAuthorId()),item.getVersion())).toList();
 }
 @Transactional(readOnly=true) public AnnouncementPublicContentResponse publishedContent(String id){
  Announcement item=find(id); if(!item.isPublished()) throw ApiException.notFound();
  return new AnnouncementPublicContentResponse(item.getTitle(),item.getContent(),users.displayNamesById(List.of(item.getAuthorId())).get(item.getAuthorId()),item.getVersion());
 }
 @Transactional(readOnly=true) public List<AnnouncementResponse> all(){return announcements.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream().map(AnnouncementResponse::from).toList();}
 @Transactional(readOnly=true) public AnnouncementResponse get(String id){return AnnouncementResponse.from(find(id));}
 @Transactional public AnnouncementResponse create(String actor,AnnouncementRequest request){Announcement item=announcements.save(new Announcement(request.title().trim(),request.content().trim(),actor)); audit.event("ANNOUNCEMENT_CREATED","Announcement",item.getId()); return AnnouncementResponse.from(item);}
 @Transactional public AnnouncementResponse update(String id,AnnouncementRequest request){Announcement item=find(id);item.update(request.title().trim(),request.content().trim());audit.event("ANNOUNCEMENT_UPDATED","Announcement",id);return AnnouncementResponse.from(item);}
 @Transactional public AnnouncementResponse publish(String id,boolean publish){Announcement item=find(id);if(publish)item.publish();else item.unpublish();audit.event(publish?"ANNOUNCEMENT_PUBLISHED":"ANNOUNCEMENT_UNPUBLISHED","Announcement",id);return AnnouncementResponse.from(item);}
 @Transactional public void delete(String id){announcements.delete(find(id));audit.event("ANNOUNCEMENT_DELETED","Announcement",id);}
 private Announcement find(String id){return announcements.findById(id).orElseThrow(ApiException::notFound);}
}
