package com.zerotrust.dto;

import java.time.LocalDateTime;

public class SessionDto {
    private String sessionId;
    private String deviceLabel;
    private String ipAddress;
    private LocalDateTime startedAt;
    private boolean current;

    public String getSessionId()                     { return sessionId; }
    public void setSessionId(String v)               { this.sessionId = v; }
    public String getDeviceLabel()                   { return deviceLabel; }
    public void setDeviceLabel(String v)             { this.deviceLabel = v; }
    public String getIpAddress()                     { return ipAddress; }
    public void setIpAddress(String v)               { this.ipAddress = v; }
    public LocalDateTime getStartedAt()              { return startedAt; }
    public void setStartedAt(LocalDateTime v)        { this.startedAt = v; }
    public boolean isCurrent()                       { return current; }
    public void setCurrent(boolean v)                { this.current = v; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final SessionDto d = new SessionDto();
        public Builder sessionId(String v)           { d.sessionId = v; return this; }
        public Builder deviceLabel(String v)         { d.deviceLabel = v; return this; }
        public Builder ipAddress(String v)           { d.ipAddress = v; return this; }
        public Builder startedAt(LocalDateTime v)    { d.startedAt = v; return this; }
        public Builder current(boolean v)            { d.current = v; return this; }
        public SessionDto build()                    { return d; }
    }
}
