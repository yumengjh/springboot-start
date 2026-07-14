package com.yumg.starter.entities;

import com.yumg.starter.common.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions")
public class Permission extends AuditedEntity {
    @Column(nullable = false, unique = true, length = 160) private String code;
    @Column(length = 500) private String description;
    protected Permission() {}
    public Permission(String code, String description) { this.code = code; this.description = description; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public void changeDescription(String description) { this.description = description; }
}
