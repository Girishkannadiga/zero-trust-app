package com.zerotrust.dto;

public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String role;
    private boolean mfaEnabled;

    public UserDto() {}

    public UserDto(Long id, String email, String name, String role, boolean mfaEnabled) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.mfaEnabled = mfaEnabled;
    }

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }
    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }
    public String getRole()                      { return role; }
    public void setRole(String role)             { this.role = role; }
    public boolean isMfaEnabled()               { return mfaEnabled; }
    public void setMfaEnabled(boolean v)         { this.mfaEnabled = v; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final UserDto d = new UserDto();
        public Builder id(Long v)                { d.id = v; return this; }
        public Builder email(String v)           { d.email = v; return this; }
        public Builder name(String v)            { d.name = v; return this; }
        public Builder role(String v)            { d.role = v; return this; }
        public Builder mfaEnabled(boolean v)     { d.mfaEnabled = v; return this; }
        public UserDto build()                   { return d; }
    }
}
