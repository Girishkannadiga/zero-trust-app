package com.zerotrust.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "active_sessions")
public class ActiveSession {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_label", length = 100)
    private String deviceLabel;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_fingerprint", length = 64)
    private String deviceFingerprint;

    @CreationTimestamp
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "is_current")
    private boolean current = false;

    protected ActiveSession() {}

    public String getId()                           { return id; }
    public void setId(String id)                    { this.id = id; }
    public User getUser()                           { return user; }
    public void setUser(User user)                  { this.user = user; }
    public String getDeviceLabel()                  { return deviceLabel; }
    public void setDeviceLabel(String v)            { this.deviceLabel = v; }
    public String getIpAddress()                    { return ipAddress; }
    public void setIpAddress(String v)              { this.ipAddress = v; }
    public String getDeviceFingerprint()            { return deviceFingerprint; }
    public void setDeviceFingerprint(String v)      { this.deviceFingerprint = v; }
    public LocalDateTime getStartedAt()             { return startedAt; }
    public LocalDateTime getExpiresAt()             { return expiresAt; }
    public void setExpiresAt(LocalDateTime v)       { this.expiresAt = v; }
    public boolean isRevoked()                      { return revoked; }
    public void setRevoked(boolean revoked)         { this.revoked = revoked; }
    public boolean isCurrent()                      { return current; }
    public void setCurrent(boolean current)         { this.current = current; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ActiveSession s = new ActiveSession();
        public Builder id(String v)                 { s.id = v; return this; }
        public Builder user(User v)                 { s.user = v; return this; }
        public Builder deviceLabel(String v)        { s.deviceLabel = v; return this; }
        public Builder ipAddress(String v)          { s.ipAddress = v; return this; }
        public Builder deviceFingerprint(String v)  { s.deviceFingerprint = v; return this; }
        public Builder expiresAt(LocalDateTime v)   { s.expiresAt = v; return this; }
        public Builder revoked(boolean v)           { s.revoked = v; return this; }
        public Builder current(boolean v)           { s.current = v; return this; }
        public ActiveSession build()                { return s; }
    }
}
