package com.yumg.starter.modules.announcements.api;
import com.yumg.starter.modules.announcements.api.dto.*;
import com.yumg.starter.modules.announcements.application.AnnouncementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/announcements") public class AnnouncementController {
 private final AnnouncementService announcements; public AnnouncementController(AnnouncementService announcements){this.announcements=announcements;}
 @GetMapping public List<AnnouncementResponse> published(){return announcements.published();}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('example:announcement:read')") public AnnouncementResponse get(@PathVariable String id){return announcements.get(id);}
 @PostMapping @PreAuthorize("hasAuthority('example:announcement:write')") public ResponseEntity<AnnouncementResponse> create(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody AnnouncementRequest request){return ResponseEntity.status(201).body(announcements.create(jwt.getSubject(),request));}
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('example:announcement:write')") public AnnouncementResponse update(@PathVariable String id,@Valid @RequestBody AnnouncementRequest request){return announcements.update(id,request);}
 @PutMapping("/{id}/publication") @PreAuthorize("hasAuthority('example:announcement:write')") public AnnouncementResponse publication(@PathVariable String id,@RequestParam boolean published){return announcements.publish(id,published);}
 @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('example:announcement:delete')") public ResponseEntity<Void> delete(@PathVariable String id){announcements.delete(id);return ResponseEntity.noContent().build();}
}
