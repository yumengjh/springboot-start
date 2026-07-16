package com.yumg.starter.modules.sqlconsole.api.dto; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.Size;
public record SqlConsoleRequest(@NotBlank @Size(max=20000) String sql, boolean confirmDangerous) {}
