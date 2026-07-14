package com.yumg.starter.entities;

import com.yumg.starter.common.entity.AuditedEntity;
import com.yumg.starter.common.entity.InstantStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ip_access_rules")
public class IpAccessRule extends AuditedEntity {
    public enum Type { ALLOW, DENY }
    @Enumerated(EnumType.STRING) @Column(name = "rule_type", nullable = false, length = 32) private Type type;
    @Column(nullable = false, length = 64) private String network;
    @Column(nullable = false, length = 64) private String scope;
    @Column(name = "expires_at") @Convert(converter = InstantStringConverter.class) private Instant expiresAt;
    @Column(length = 500) private String reason;
    protected IpAccessRule() { }
    public IpAccessRule(Type type, String network, String scope, Instant expiresAt, String reason) { this.type=type; this.network=network; this.scope=scope; this.expiresAt=expiresAt; this.reason=reason; }
    public Type getType(){return type;} public String getNetwork(){return network;} public String getScope(){return scope;} public Instant getExpiresAt(){return expiresAt;} public String getReason(){return reason;}
    public boolean activeAt(Instant now){return expiresAt==null||expiresAt.isAfter(now);}
}
