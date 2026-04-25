package com.zerotrust.service;

import com.zerotrust.dto.DashboardStatsDto;
import com.zerotrust.dto.LoginLogDto;
import com.zerotrust.dto.RiskScoreDto;
import com.zerotrust.dto.SecurityStatusDto;
import com.zerotrust.model.User;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserService userService;
    private final DeviceTrackingService deviceTrackingService;
    private final IpTrackingService ipTrackingService;
    private final LoginLogService loginLogService;
    private final SessionService sessionService;

    public DashboardService(UserService userService,
                            DeviceTrackingService deviceTrackingService,
                            IpTrackingService ipTrackingService,
                            LoginLogService loginLogService,
                            SessionService sessionService) {
        this.userService = userService;
        this.deviceTrackingService = deviceTrackingService;
        this.ipTrackingService = ipTrackingService;
        this.loginLogService = loginLogService;
        this.sessionService = sessionService;
    }

    public SecurityStatusDto getSecurityStatus(String email, String currentIp, String deviceFingerprint) {
        User user = userService.getByEmail(email);

        boolean deviceTrusted = deviceTrackingService.isTrusted(user, deviceFingerprint);
        boolean ipSuspicious  = ipTrackingService.isSuspicious(user, currentIp);

        String deviceLabel = deviceTrackingService.getUserDevices(user).stream()
                .filter(d -> d.getFingerprint().equals(deviceFingerprint))
                .findFirst()
                .map(d -> d.getLabel())
                .orElse("Unknown Device");

        List<LoginLogDto> logs = loginLogService.getRecentLogs(user, 1);
        String lastLogin = logs.isEmpty() ? null
                : logs.get(0).getTimestamp().format(FORMATTER);

        boolean newIp = ipTrackingService.isNewIp(user, currentIp);

        return SecurityStatusDto.builder()
                .mfaEnabled(user.isMfaEnabled())
                .deviceTrusted(deviceTrusted)
                .ipSuspicious(ipSuspicious)
                .isNewIp(newIp)
                .deviceLabel(deviceLabel)
                .currentIp(currentIp)
                .lastLogin(lastLogin)
                .build();
    }

    public DashboardStatsDto getStats(String email) {
        User user = userService.getByEmail(email);
        return DashboardStatsDto.builder()
                .activeSessions(sessionService.countActiveSessions(user))
                .trustedDevices(deviceTrackingService.countTrustedDevices(user))
                .uniqueIps(ipTrackingService.countUniqueIpsLast30Days(user))
                .blockedAttempts(loginLogService.countFailedAttempts(user))
                .untrustedDevices(deviceTrackingService.countUntrustedDevices(user))
                .build();
    }

    public RiskScoreDto getRiskScore(String email, String currentIp, String deviceFingerprint) {
        User user = userService.getByEmail(email);
        List<String> factors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        int score = 0;

        if (!user.isMfaEnabled()) {
            score += 30;
            factors.add("MFA is disabled on your account");
            recommendations.add("Enable Multi-Factor Authentication for stronger security");
        }

        boolean deviceTrusted = deviceTrackingService.isTrusted(user, deviceFingerprint);
        if (!deviceTrusted) {
            score += 25;
            factors.add("Current device is not in trusted devices list");
            recommendations.add("Mark this device as trusted after verifying it is yours");
        }

        if (ipTrackingService.isSuspicious(user, currentIp)) {
            score += 20;
            factors.add("Current IP address is flagged as suspicious");
            recommendations.add("Review recent logins and revoke suspicious sessions");
        }

        long failedAttempts = loginLogService.countFailedAttempts(user);
        if (failedAttempts > 2) {
            score += 15;
            factors.add(failedAttempts + " failed login attempts recorded");
            recommendations.add("Review login logs for unauthorized access attempts");
        }

        long activeSessions = sessionService.countActiveSessions(user);
        if (activeSessions > 2) {
            score += 10;
            factors.add(activeSessions + " concurrent active sessions");
            recommendations.add("Revoke unused sessions to minimise attack surface");
        }

        if (factors.isEmpty()) {
            factors.add("No risk factors detected — account looks secure");
        }

        int finalScore = Math.min(score, 100);
        String level = finalScore >= 61 ? "HIGH" : finalScore >= 31 ? "MEDIUM" : "LOW";

        return RiskScoreDto.builder()
                .score(finalScore)
                .level(level)
                .factors(factors)
                .recommendations(recommendations)
                .build();
    }
}
