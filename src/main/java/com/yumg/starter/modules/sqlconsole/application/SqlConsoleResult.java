package com.yumg.starter.modules.sqlconsole.application;
import java.util.List; import java.util.Map;
public record SqlConsoleResult(boolean query, int updateCount, List<Map<String,Object>> rows) {}
