package com.zerotrust.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String deviceFingerprint;
    private String deviceLabel;
    private String deviceOs;
    private String deviceBrowser;

    public String getEmail()                          { return email; }
    public void setEmail(String email)               { this.email = email; }
    public String getPassword()                      { return password; }
    public void setPassword(String password)         { this.password = password; }
    public String getDeviceFingerprint()             { return deviceFingerprint; }
    public void setDeviceFingerprint(String v)       { this.deviceFingerprint = v; }
    public String getDeviceLabel()                   { return deviceLabel; }
    public void setDeviceLabel(String v)             { this.deviceLabel = v; }
    public String getDeviceOs()                      { return deviceOs; }
    public void setDeviceOs(String v)                { this.deviceOs = v; }
    public String getDeviceBrowser()                 { return deviceBrowser; }
    public void setDeviceBrowser(String v)           { this.deviceBrowser = v; }
}
