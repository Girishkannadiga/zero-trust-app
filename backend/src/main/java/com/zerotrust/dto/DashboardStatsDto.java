package com.zerotrust.dto;

public class DashboardStatsDto {
    private long activeSessions;
    private long trustedDevices;
    private long uniqueIps;
    private long blockedAttempts;
    private long untrustedDevices;

    public long getActiveSessions()                  { return activeSessions; }
    public void setActiveSessions(long v)            { this.activeSessions = v; }
    public long getTrustedDevices()                  { return trustedDevices; }
    public void setTrustedDevices(long v)            { this.trustedDevices = v; }
    public long getUniqueIps()                       { return uniqueIps; }
    public void setUniqueIps(long v)                 { this.uniqueIps = v; }
    public long getBlockedAttempts()                 { return blockedAttempts; }
    public void setBlockedAttempts(long v)           { this.blockedAttempts = v; }
    public long getUntrustedDevices()                { return untrustedDevices; }
    public void setUntrustedDevices(long v)          { this.untrustedDevices = v; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final DashboardStatsDto d = new DashboardStatsDto();
        public Builder activeSessions(long v)        { d.activeSessions = v; return this; }
        public Builder trustedDevices(long v)        { d.trustedDevices = v; return this; }
        public Builder uniqueIps(long v)             { d.uniqueIps = v; return this; }
        public Builder blockedAttempts(long v)       { d.blockedAttempts = v; return this; }
        public Builder untrustedDevices(long v)      { d.untrustedDevices = v; return this; }
        public DashboardStatsDto build()             { return d; }
    }
}
