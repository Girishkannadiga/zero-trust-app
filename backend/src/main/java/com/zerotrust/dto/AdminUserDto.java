package com.zerotrust.dto;

import java.time.LocalDateTime;

public class AdminUserDto {

    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean mfaEnabled;
    private boolean accountLocked;
    private int failedAttempts;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private String lastDevice;
    private boolean deviceTrusted;
    private long activeSessions;

    public AdminUserDto() {}

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }
    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }
    public String getEmail()                       { return email; }
    public void setEmail(String email)             { this.email = email; }
    public String getRole()                        { return role; }
    public void setRole(String role)               { this.role = role; }
    public boolean isMfaEnabled()                  { return mfaEnabled; }
    public void setMfaEnabled(boolean v)           { this.mfaEnabled = v; }
    public boolean isAccountLocked()               { return accountLocked; }
    public void setAccountLocked(boolean v)        { this.accountLocked = v; }
    public int getFailedAttempts()                 { return failedAttempts; }
    public void setFailedAttempts(int v)           { this.failedAttempts = v; }
    public String getLastLoginIp()                 { return lastLoginIp; }
    public void setLastLoginIp(String v)           { this.lastLoginIp = v; }
    public LocalDateTime getLastLoginTime()        { return lastLoginTime; }
    public void setLastLoginTime(LocalDateTime v)  { this.lastLoginTime = v; }
    public String getLastDevice()                  { return lastDevice; }
    public void setLastDevice(String v)            { this.lastDevice = v; }
    public boolean isDeviceTrusted()               { return deviceTrusted; }
    public void setDeviceTrusted(boolean v)        { this.deviceTrusted = v; }
    public long getActiveSessions()                { return activeSessions; }
    public void setActiveSessions(long v)          { this.activeSessions = v; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AdminUserDto d = new AdminUserDto();
        public Builder id(Long v)                  { d.id = v; return this; }
        public Builder name(String v)              { d.name = v; return this; }
        public Builder email(String v)             { d.email = v; return this; }
        public Builder role(String v)              { d.role = v; return this; }
        public Builder mfaEnabled(boolean v)       { d.mfaEnabled = v; return this; }
        public Builder accountLocked(boolean v)    { d.accountLocked = v; return this; }
        public Builder failedAttempts(int v)       { d.failedAttempts = v; return this; }
        public Builder lastLoginIp(String v)       { d.lastLoginIp = v; return this; }
        public Builder lastLoginTime(LocalDateTime v) { d.lastLoginTime = v; return this; }
        public Builder lastDevice(String v)        { d.lastDevice = v; return this; }
        public Builder deviceTrusted(boolean v)    { d.deviceTrusted = v; return this; }
        public Builder activeSessions(long v)      { d.activeSessions = v; return this; }
        public AdminUserDto build()                { return d; }
    }
}
