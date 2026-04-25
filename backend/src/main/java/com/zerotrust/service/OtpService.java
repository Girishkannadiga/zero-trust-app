package com.zerotrust.service;

import com.zerotrust.exception.OtpExpiredException;
import com.zerotrust.model.OtpRecord;
import com.zerotrust.repository.OtpRepository;
import com.zerotrust.util.OtpGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_VALID_MINUTES = 10;

    private final OtpRepository otpRepository;
    private final OtpGenerator otpGenerator;

    public OtpService(OtpRepository otpRepository, OtpGenerator otpGenerator) {
        this.otpRepository = otpRepository;
        this.otpGenerator = otpGenerator;
    }

    @Transactional
    public String generateAndSave(String email, String method) {
        otpRepository.invalidateAllForEmail(email, method);

        String code = otpGenerator.generate();
        otpRepository.save(OtpRecord.builder()
                .email(email)
                .code(code)
                .method(method)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES))
                .used(false)
                .build());

        log.info("OTP generated for {} via {}", email, method);
        log.info("==================================================");
        log.info("  DEMO OTP for [{}]: {}", email, code);
        log.info("==================================================");
        return code;
    }

    @Transactional
    public void verify(String email, String code, String method) {
        OtpRecord record = otpRepository
                .findTopByEmailAndMethodAndUsedFalseOrderByCreatedAtDesc(email, method)
                .orElseThrow(() -> new OtpExpiredException("No active OTP found. Please request a new code."));

        if (LocalDateTime.now().isAfter(record.getExpiresAt())) {
            throw new OtpExpiredException("OTP has expired. Please request a new code.");
        }

        if (!record.getCode().equals(code)) {
            throw new OtpExpiredException("Invalid OTP code. Please try again.");
        }

        record.setUsed(true);
        otpRepository.save(record);
    }
}
