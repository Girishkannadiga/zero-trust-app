package com.zerotrust.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ip_records",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "ip_address"}))
public class IpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(length = 60)
    private String country;

    @Column(length = 60)
    private String city;

    @Column(nullable = false)
    private boolean trusted = false;

    @Column(nullable = false)
    private boolean suspicious = false;

    @Column(name = "access_count")
    private int accessCount = 1;

    @CreationTimestamp
    @Column(name = "first_seen")
    private LocalDateTime firstSeen;

    @UpdateTimestamp
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    protected IpRecord() {}

    public Long getId()                        { return id; }
    public User getUser()                      { return user; }
    public void setUser(User user)             { this.user = user; }
    public String getIpAddress()               { return ipAddress; }
    public void setIpAddress(String v)         { this.ipAddress = v; }
    public String getCountry()                 { return country; }
    public void setCountry(String country)     { this.country = country; }
    public String getCity()                    { return city; }
    public void setCity(String city)           { this.city = city; }
    public boolean isTrusted()                 { return trusted; }
    public void setTrusted(boolean trusted)    { this.trusted = trusted; }
    public boolean isSuspicious()              { return suspicious; }
    public void setSuspicious(boolean v)       { this.suspicious = v; }
    public int getAccessCount()                { return accessCount; }
    public void setAccessCount(int v)          { this.accessCount = v; }
    public LocalDateTime getFirstSeen()        { return firstSeen; }
    public LocalDateTime getLastSeen()         { return lastSeen; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final IpRecord r = new IpRecord();
        public Builder user(User v)            { r.user = v; return this; }
        public Builder ipAddress(String v)     { r.ipAddress = v; return this; }
        public Builder trusted(boolean v)      { r.trusted = v; return this; }
        public Builder suspicious(boolean v)   { r.suspicious = v; return this; }
        public Builder accessCount(int v)      { r.accessCount = v; return this; }
        public IpRecord build()                { return r; }
    }
}
