package com.yumg.starter.entities;

import com.yumg.starter.common.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import com.yumg.starter.common.entity.InstantStringConverter;
import java.time.Instant;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends AuditedEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "display_name", nullable = false, length = 160)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserStatus status;

    @Column(name = "token_version", nullable = false)
    private long tokenVersion;
    @Column(name = "locked_until")
    @Convert(converter = InstantStringConverter.class)
    private Instant lockedUntil;
    @ManyToMany
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    protected User() {
    }

    public User(String username, String displayName, String passwordHash) {
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.status = UserStatus.ACTIVE;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public long getTokenVersion() {
        return tokenVersion;
    }

    public void changeDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.tokenVersion++;
    }
    public void invalidateSessions() { tokenVersion++; }
    public void grant(Role role) { roles.add(role); }
    public void revoke(Role role) { roles.remove(role); }
    public Set<Role> getRoles() { return Set.copyOf(roles); }
    public boolean isSuperAdmin() { return roles.stream().anyMatch(role -> "SUPER_ADMIN".equals(role.getCode())); }
    public void lock(Instant until) { status = UserStatus.LOCKED; lockedUntil = until; tokenVersion++; }
    public void unlock() { if (status == UserStatus.LOCKED) { status = UserStatus.ACTIVE; lockedUntil = null; tokenVersion++; } }
    public void disable() { status = UserStatus.DISABLED; tokenVersion++; }
    public void enable() { if (status == UserStatus.DISABLED) { status = UserStatus.ACTIVE; tokenVersion++; } }
    public boolean unlockIfExpired(Instant now) {
        if (status == UserStatus.LOCKED && lockedUntil != null && !lockedUntil.isAfter(now)) { unlock(); return true; }
        return false;
    }
}
