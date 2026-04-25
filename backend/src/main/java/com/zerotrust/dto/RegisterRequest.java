package com.zerotrust.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public RegisterRequest() {}

    public String getName()             { return name; }
    public String getEmail()            { return email; }
    public String getPassword()         { return password; }

    public void setName(String name)       { this.name = name; }
    public void setEmail(String email)     { this.email = email; }
    public void setPassword(String p)      { this.password = p; }
}
