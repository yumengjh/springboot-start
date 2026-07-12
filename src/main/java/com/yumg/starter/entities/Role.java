package com.yumg.starter.entities;

import com.yumg.starter.common.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role extends AuditedEntity {
    @Column(nullable = false, unique = true, length = 100) private String code;
    @Column(name = "display_name", nullable = false, length = 160) private String displayName;
    @ManyToMany
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new LinkedHashSet<>();
    protected Role() {}
    public Role(String code, String displayName) { this.code = code; this.displayName = displayName; }
    public String getCode() { return code; }
    public Set<Permission> getPermissions() { return Set.copyOf(permissions); }
    public void grant(Permission permission) { permissions.add(permission); }
    public void revoke(Permission permission) { permissions.remove(permission); }
}
