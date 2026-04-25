package com.zerotrust.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    public String generate() {
        int upperBound = (int) Math.pow(10, OTP_LENGTH);
        int otp = SECURE_RANDOM.nextInt(upperBound);
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }
}
