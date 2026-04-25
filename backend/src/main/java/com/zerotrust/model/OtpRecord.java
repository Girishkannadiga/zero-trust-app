package com.zerotrust.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_records")
public class OtpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used")
    private boolean used = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    protected OtpRecord() {}

    public Long getId()                          { return id; }
    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }
    public String getCode()                      { return code; }
    public void setCode(String code)             { this.code = code; }
    public String getMethod()                    { return method; }
    public void setMethod(String method)         { this.method = method; }
    public LocalDateTime getExpiresAt()          { return expiresAt; }
    public void setExpiresAt(LocalDateTime v)    { this.expiresAt = v; }
    public boolean isUsed()                      { return used; }
    public void setUsed(boolean used)            { this.used = used; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final OtpRecord r = new OtpRecord();
        public Builder email(String v)           { r.email = v; return this; }
        public Builder code(String v)            { r.code = v; return this; }
        public Builder method(String v)          { r.method = v; return this; }
        public Builder expiresAt(LocalDateTime v){ r.expiresAt = v; return this; }
        public Builder used(boolean v)           { r.used = v; return this; }
        public OtpRecord build()                 { return r; }
    }
}
