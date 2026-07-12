package com.yumg.starter.entities;

import com.yumg.starter.common.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_settings")
public class RuntimeSetting extends AuditedEntity {
    @Column(name = "setting_key", nullable = false, unique = true, length = 160)
    private String key;
    @Column(name = "value_type", nullable = false, length = 32)
    private String valueType;
    @Column(name = "value_json", nullable = false, columnDefinition = "text")
    private String value;

    protected RuntimeSetting() {}
    public RuntimeSetting(String key, String valueType, String value) {
        this.key = key; this.valueType = valueType; this.value = value;
    }
    public String getKey() { return key; }
    public String getValueType() { return valueType; }
    public String getValue() { return value; }
    public void changeValue(String value) { this.value = value; }
}
