package com.yumg.starter.modules.announcements.api.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AnnouncementRequest(@NotBlank @Size(max = 160) String title, @NotBlank @Size(max = 20000) String content) { }
