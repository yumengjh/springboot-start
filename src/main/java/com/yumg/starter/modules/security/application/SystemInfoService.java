package com.yumg.starter.modules.security.application;

import com.yumg.starter.modules.security.api.dto.SystemInfoResponse;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class SystemInfoService {
    private final BuildProperties build; private final GitProperties git;
    public SystemInfoService(Optional<BuildProperties> build, Optional<GitProperties> git) { this.build=build.orElse(null); this.git=git.orElse(null); }
    public SystemInfoResponse info() {
        return new SystemInfoResponse(build == null ? null : build.getVersion(), gitCommit(),
                build == null || build.getTime() == null ? null : build.getTime().toEpochMilli());
    }
    private String gitCommit() {
        if (git == null) return null;
        return git.getCommitId() != null ? git.getCommitId() : git.getShortCommitId();
    }
}
