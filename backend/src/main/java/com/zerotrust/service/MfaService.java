package com.zerotrust.service;

import com.zerotrust.exception.OtpExpiredException;
import com.zerotrust.model.User;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final OtpService otpService;
    private final MailService mailService;

    public MfaService(OtpService otpService, MailService mailService) {
        this.otpService = otpService;
        this.mailService = mailService;
    }

    public void sendOtp(User user, String method) {
        if ("EMAIL".equalsIgnoreCase(method)) {
            String code = otpService.generateAndSave(user.getEmail(), "EMAIL");
            mailService.sendOtpEmail(user.getEmail(), user.getName(), code);
            return;
        }
        // TOTP (authenticator app) codes are generated client-side — server only validates
        if (!"TOTP".equalsIgnoreCase(method)) {
            throw new OtpExpiredException("Unknown MFA method: " + method);
        }
    }

    public void verifyOtp(String email, String code, String method) {
        if ("EMAIL".equalsIgnoreCase(method) || "TOTP".equalsIgnoreCase(method)) {
            otpService.verify(email, code, method.toUpperCase());
            return;
        }
        throw new OtpExpiredException("Unknown MFA method: " + method);
    }
}
