package com.yumg.starter.modules.announcements.api;

import com.yumg.starter.modules.announcements.api.dto.*;
import com.yumg.starter.modules.announcements.application.AnnouncementService;
import com.yumg.starter.common.web.PublicApi;
import com.yumg.starter.common.web.PublicApiAccess;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/announcements")
@Tag(name = "公告示例")
public class AnnouncementController {
    private final AnnouncementService announcements;

    public AnnouncementController(AnnouncementService announcements) {
        this.announcements = announcements;
    }

    @GetMapping({"", "/"})
    @PublicApi(minimalResponse = true, detailedAuthority = "example:announcement:read")
    @Operation(summary = "读取已发布公告列表（公开）")
    public Object published() {
        return canReadDetails() ? announcements.all() : announcements.published();
    }

    @GetMapping("/manage")
    @PreAuthorize("hasAuthority('example:announcement:read')")
    public List<AnnouncementResponse> all() {
        return announcements.all();
    }

    @GetMapping("/manage/{id}")
    @PreAuthorize("hasAuthority('example:announcement:read')")
    public AnnouncementResponse managedGet(@PathVariable String id) {
        return announcements.get(id);
    }

    @GetMapping({"/{id}", "/{id}/"})
    @PublicApi(minimalResponse = true, detailedAuthority = "example:announcement:read")
    @Operation(summary = "读取已发布公告内容（公开）")
    public Object get(@PathVariable String id) {
        return canReadDetails() ? announcements.get(id) : announcements.publishedContent(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('example:announcement:write')")
    public ResponseEntity<AnnouncementResponse> create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.status(201).body(announcements.create(jwt.getSubject(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('example:announcement:write')")
    public AnnouncementResponse update(@PathVariable String id, @Valid @RequestBody AnnouncementRequest request) {
        return announcements.update(id, request);
    }

    @PutMapping("/{id}/publication")
    @PreAuthorize("hasAuthority('example:announcement:write')")
    public AnnouncementResponse publication(@PathVariable String id, @RequestParam boolean published) {
        return announcements.publish(id, published);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('example:announcement:delete')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        announcements.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean canReadDetails() {
        return PublicApiAccess.hasAuthority("example:announcement:read");
    }
}
