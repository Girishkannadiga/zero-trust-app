package com.zerotrust.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_info",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "fingerprint"}))
public class DeviceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 64)
    private String fingerprint;

    @Column(length = 100)
    private String label;

    @Column(length = 50)
    private String os;

    @Column(length = 50)
    private String browser;

    @Column(nullable = false)
    private boolean trusted = false;

    @CreationTimestamp
    @Column(name = "first_seen")
    private LocalDateTime firstSeen;

    @UpdateTimestamp
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    protected DeviceInfo() {}

    public Long getId()                        { return id; }
    public User getUser()                      { return user; }
    public void setUser(User user)             { this.user = user; }
    public String getFingerprint()             { return fingerprint; }
    public void setFingerprint(String v)       { this.fingerprint = v; }
    public String getLabel()                   { return label; }
    public void setLabel(String label)         { this.label = label; }
    public String getOs()                      { return os; }
    public void setOs(String os)               { this.os = os; }
    public String getBrowser()                 { return browser; }
    public void setBrowser(String browser)     { this.browser = browser; }
    public boolean isTrusted()                 { return trusted; }
    public void setTrusted(boolean trusted)    { this.trusted = trusted; }
    public LocalDateTime getFirstSeen()        { return firstSeen; }
    public LocalDateTime getLastSeen()         { return lastSeen; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final DeviceInfo d = new DeviceInfo();
        public Builder user(User v)            { d.user = v; return this; }
        public Builder fingerprint(String v)   { d.fingerprint = v; return this; }
        public Builder label(String v)         { d.label = v; return this; }
        public Builder os(String v)            { d.os = v; return this; }
        public Builder browser(String v)       { d.browser = v; return this; }
        public Builder trusted(boolean v)      { d.trusted = v; return this; }
        public DeviceInfo build()              { return d; }
    }
}
