package com.yumg.starter.modules.security.api;

import com.yumg.starter.common.web.PublicApi;
import com.yumg.starter.modules.security.api.dto.SystemInfoResponse;
import com.yumg.starter.modules.security.application.SystemInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController @RequestMapping("/api/v1/system/info")
@Tag(name = "系统信息")
public class SystemInfoController {
    private final SystemInfoService info; public SystemInfoController(SystemInfoService info){this.info=info;}
    @GetMapping @PublicApi @Operation(summary = "读取构建与版本信息") public SystemInfoResponse get(){return info.info();}
}
