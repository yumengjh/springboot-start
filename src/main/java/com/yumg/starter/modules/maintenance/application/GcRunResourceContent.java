package com.yumg.starter.modules.maintenance.application;

/** A single managed resource's measurable outcome in one GC run. */
public record GcRunResourceContent(String resourceCode, long candidates, long deleted, String message) {
    static GcRunResourceContent from(GcResourceResult result) {
        return new GcRunResourceContent(result.resourceCode(), result.candidates(), result.deleted(), result.message());
    }
}
