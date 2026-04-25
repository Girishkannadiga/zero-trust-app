package com.zerotrust.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequest {
    @NotBlank(message = "Name cannot be empty.")
    private String name;

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
}
