package com.zerotrust.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Method is required")
    private String method; // EMAIL or TOTP

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }
    public String getMethod()               { return method; }
    public void setMethod(String method)    { this.method = method; }
}
