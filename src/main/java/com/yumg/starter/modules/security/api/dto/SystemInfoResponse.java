package com.yumg.starter.modules.security.api.dto;

public record SystemInfoResponse(String version, String gitCommit, Long buildTime) { }
