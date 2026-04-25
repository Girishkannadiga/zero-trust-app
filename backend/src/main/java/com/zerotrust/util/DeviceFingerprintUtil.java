package com.zerotrust.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DeviceFingerprintUtil {

    private static final int FINGERPRINT_LENGTH = 64;
    private static final String FINGERPRINT_PATTERN = "[a-f0-9]{64}";

    public boolean isValid(String fingerprint) {
        return StringUtils.hasText(fingerprint) && fingerprint.matches(FINGERPRINT_PATTERN);
    }

    public String sanitize(String fingerprint) {
        if (!StringUtils.hasText(fingerprint)) return "";
        String cleaned = fingerprint.toLowerCase().replaceAll("[^a-f0-9]", "");
        return cleaned.substring(0, Math.min(FINGERPRINT_LENGTH, cleaned.length()));
    }
}
