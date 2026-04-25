package com.zerotrust.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = true;

    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected User() {}

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }
    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }
    public Role getRole()                      { return role; }
    public void setRole(Role role)             { this.role = role; }
    public boolean isEnabled()                 { return enabled; }
    public void setEnabled(boolean enabled)    { this.enabled = enabled; }
    public boolean isMfaEnabled()              { return mfaEnabled; }
    public void setMfaEnabled(boolean v)       { this.mfaEnabled = v; }
    public boolean isAccountLocked()           { return accountLocked; }
    public void setAccountLocked(boolean v)    { this.accountLocked = v; }
    public int getFailedLoginAttempts()        { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int v)  { this.failedLoginAttempts = v; }
    public LocalDateTime getLockTime()         { return lockTime; }
    public void setLockTime(LocalDateTime v)   { this.lockTime = v; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public LocalDateTime getUpdatedAt()        { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final User u = new User();
        public Builder id(Long v)                  { u.id = v; return this; }
        public Builder email(String v)             { u.email = v; return this; }
        public Builder password(String v)          { u.password = v; return this; }
        public Builder name(String v)              { u.name = v; return this; }
        public Builder role(Role v)                { u.role = v; return this; }
        public Builder enabled(boolean v)          { u.enabled = v; return this; }
        public Builder mfaEnabled(boolean v)       { u.mfaEnabled = v; return this; }
        public Builder accountLocked(boolean v)    { u.accountLocked = v; return this; }
        public Builder failedLoginAttempts(int v)  { u.failedLoginAttempts = v; return this; }
        public User build() { return u; }
    }
}
