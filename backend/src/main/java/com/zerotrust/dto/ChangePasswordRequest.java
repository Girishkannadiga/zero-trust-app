package com.zerotrust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 6, message = "New password must be at least 6 characters.")
    private String newPassword;

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String v) { this.oldPassword = v; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String v) { this.newPassword = v; }
}
