package com.zerotrust.dto;

import java.time.LocalDateTime;

public class LoginLogDto {
    private Long id;
    private String ipAddress;
    private String deviceLabel;
    private String location;
    private boolean success;
    private boolean mfaUsed;
    private LocalDateTime timestamp;

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public String getIpAddress()                 { return ipAddress; }
    public void setIpAddress(String v)           { this.ipAddress = v; }
    public String getDeviceLabel()               { return deviceLabel; }
    public void setDeviceLabel(String v)         { this.deviceLabel = v; }
    public String getLocation()                  { return location; }
    public void setLocation(String v)            { this.location = v; }
    public boolean isSuccess()                   { return success; }
    public void setSuccess(boolean v)            { this.success = v; }
    public boolean isMfaUsed()                   { return mfaUsed; }
    public void setMfaUsed(boolean v)            { this.mfaUsed = v; }
    public LocalDateTime getTimestamp()          { return timestamp; }
    public void setTimestamp(LocalDateTime v)    { this.timestamp = v; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final LoginLogDto d = new LoginLogDto();
        public Builder id(Long v)                { d.id = v; return this; }
        public Builder ipAddress(String v)       { d.ipAddress = v; return this; }
        public Builder deviceLabel(String v)     { d.deviceLabel = v; return this; }
        public Builder location(String v)        { d.location = v; return this; }
        public Builder success(boolean v)        { d.success = v; return this; }
        public Builder mfaUsed(boolean v)        { d.mfaUsed = v; return this; }
        public Builder timestamp(LocalDateTime v){ d.timestamp = v; return this; }
        public LoginLogDto build()               { return d; }
    }
}
