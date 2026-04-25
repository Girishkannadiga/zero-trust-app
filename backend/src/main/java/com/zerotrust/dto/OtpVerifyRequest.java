package com.zerotrust.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OtpVerifyRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP code is required")
    private String otp;

    @NotBlank(message = "Method is required")
    private String method;

    private String deviceFingerprint;
    private boolean rememberDevice;

    public String getEmail()                          { return email; }
    public void setEmail(String email)               { this.email = email; }
    public String getOtp()                           { return otp; }
    public void setOtp(String otp)                   { this.otp = otp; }
    public String getMethod()                        { return method; }
    public void setMethod(String method)             { this.method = method; }
    public String getDeviceFingerprint()             { return deviceFingerprint; }
    public void setDeviceFingerprint(String v)       { this.deviceFingerprint = v; }
    public boolean isRememberDevice()                { return rememberDevice; }
    public void setRememberDevice(boolean v)         { this.rememberDevice = v; }
}
