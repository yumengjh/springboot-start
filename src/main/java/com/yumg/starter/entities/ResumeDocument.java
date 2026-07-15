package com.yumg.starter.entities;

import com.yumg.starter.common.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "resume_documents")
public class ResumeDocument extends AuditedEntity {

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion;

    protected ResumeDocument() {
    }

    public ResumeDocument(String content, int schemaVersion) {
        replace(content, schemaVersion);
    }

    public String getContent() {
        return content;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void replace(String content, int schemaVersion) {
        this.content = content;
        this.schemaVersion = schemaVersion;
    }
}
