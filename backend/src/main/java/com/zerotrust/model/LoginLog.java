package com.zerotrust.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_logs")
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_label", length = 100)
    private String deviceLabel;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "mfa_used")
    private boolean mfaUsed;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @CreationTimestamp
    private LocalDateTime timestamp;

    protected LoginLog() {}

    public Long getId()                          { return id; }
    public User getUser()                        { return user; }
    public void setUser(User user)               { this.user = user; }
    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }
    public String getIpAddress()                 { return ipAddress; }
    public void setIpAddress(String v)           { this.ipAddress = v; }
    public String getDeviceLabel()               { return deviceLabel; }
    public void setDeviceLabel(String v)         { this.deviceLabel = v; }
    public String getLocation()                  { return location; }
    public void setLocation(String location)     { this.location = location; }
    public boolean isSuccess()                   { return success; }
    public void setSuccess(boolean success)      { this.success = success; }
    public boolean isMfaUsed()                   { return mfaUsed; }
    public void setMfaUsed(boolean mfaUsed)      { this.mfaUsed = mfaUsed; }
    public String getFailureReason()             { return failureReason; }
    public void setFailureReason(String v)       { this.failureReason = v; }
    public LocalDateTime getTimestamp()          { return timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final LoginLog l = new LoginLog();
        public Builder user(User v)              { l.user = v; return this; }
        public Builder email(String v)           { l.email = v; return this; }
        public Builder ipAddress(String v)       { l.ipAddress = v; return this; }
        public Builder deviceLabel(String v)     { l.deviceLabel = v; return this; }
        public Builder location(String v)        { l.location = v; return this; }
        public Builder success(boolean v)        { l.success = v; return this; }
        public Builder mfaUsed(boolean v)        { l.mfaUsed = v; return this; }
        public Builder failureReason(String v)   { l.failureReason = v; return this; }
        public LoginLog build()                  { return l; }
    }
}
