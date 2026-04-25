package com.zerotrust.dto;

public class LoginResponse {
    private String token;
    private boolean mfaRequired;
    private boolean newIpAlert;
    private UserDto user;
    private String message;

    public LoginResponse() {}

    public String getToken()                         { return token; }
    public void setToken(String token)               { this.token = token; }
    public boolean isMfaRequired()                   { return mfaRequired; }
    public void setMfaRequired(boolean mfaRequired)  { this.mfaRequired = mfaRequired; }
    public boolean isNewIpAlert()                    { return newIpAlert; }
    public void setNewIpAlert(boolean v)             { this.newIpAlert = v; }
    public UserDto getUser()                         { return user; }
    public void setUser(UserDto user)                { this.user = user; }
    public String getMessage()                       { return message; }
    public void setMessage(String message)           { this.message = message; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final LoginResponse r = new LoginResponse();
        public Builder token(String v)               { r.token = v; return this; }
        public Builder mfaRequired(boolean v)        { r.mfaRequired = v; return this; }
        public Builder newIpAlert(boolean v)         { r.newIpAlert = v; return this; }
        public Builder user(UserDto v)               { r.user = v; return this; }
        public Builder message(String v)             { r.message = v; return this; }
        public LoginResponse build()                 { return r; }
    }
}
