package com.yumg.starter.modules.maintenance.application;

/** Global controls for automatic GC. Manual runs remain available while this is disabled. */
public record GcScheduleContent(boolean automaticEnabled, int intervalMinutes) {}
