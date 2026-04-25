package com.zerotrust.dto;

public class SecurityStatusDto {
    private boolean mfaEnabled;
    private boolean deviceTrusted;
    private boolean ipSuspicious;
    private boolean isNewIp;
    private String deviceLabel;
    private String currentIp;
    private String lastLogin;

    public boolean isMfaEnabled()                    { return mfaEnabled; }
    public void setMfaEnabled(boolean v)             { this.mfaEnabled = v; }
    public boolean isDeviceTrusted()                 { return deviceTrusted; }
    public void setDeviceTrusted(boolean v)          { this.deviceTrusted = v; }
    public boolean isIpSuspicious()                  { return ipSuspicious; }
    public void setIpSuspicious(boolean v)           { this.ipSuspicious = v; }
    public boolean isNewIp()                         { return isNewIp; }
    public void setNewIp(boolean v)                  { this.isNewIp = v; }
    public String getDeviceLabel()                   { return deviceLabel; }
    public void setDeviceLabel(String v)             { this.deviceLabel = v; }
    public String getCurrentIp()                     { return currentIp; }
    public void setCurrentIp(String v)               { this.currentIp = v; }
    public String getLastLogin()                     { return lastLogin; }
    public void setLastLogin(String v)               { this.lastLogin = v; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final SecurityStatusDto d = new SecurityStatusDto();
        public Builder mfaEnabled(boolean v)         { d.mfaEnabled = v; return this; }
        public Builder deviceTrusted(boolean v)      { d.deviceTrusted = v; return this; }
        public Builder ipSuspicious(boolean v)       { d.ipSuspicious = v; return this; }
        public Builder isNewIp(boolean v)            { d.isNewIp = v; return this; }
        public Builder deviceLabel(String v)         { d.deviceLabel = v; return this; }
        public Builder currentIp(String v)           { d.currentIp = v; return this; }
        public Builder lastLogin(String v)           { d.lastLogin = v; return this; }
        public SecurityStatusDto build()             { return d; }
    }
}
