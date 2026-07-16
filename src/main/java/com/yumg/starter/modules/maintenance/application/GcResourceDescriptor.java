package com.yumg.starter.modules.maintenance.application;
public record GcResourceDescriptor(String code, String displayName, String description,
                                   int defaultRetentionDays, int defaultBatchSize,
                                   boolean automaticByDefault) {}
