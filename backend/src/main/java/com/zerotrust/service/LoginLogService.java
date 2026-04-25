package com.zerotrust.service;

import com.zerotrust.dto.LoginLogDto;
import com.zerotrust.model.LoginLog;
import com.zerotrust.model.User;
import com.zerotrust.repository.LoginLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;
    private final GeoLocationService geoLocationService;

    public LoginLogService(LoginLogRepository loginLogRepository,
                           GeoLocationService geoLocationService) {
        this.loginLogRepository = loginLogRepository;
        this.geoLocationService = geoLocationService;
    }

    public void logSuccess(User user, String ipAddress, String deviceLabel, boolean mfaUsed) {
        loginLogRepository.save(LoginLog.builder()
                .user(user)
                .email(user.getEmail())
                .ipAddress(ipAddress)
                .deviceLabel(deviceLabel)
                .location(geoLocationService.getLocation(ipAddress))
                .success(true)
                .mfaUsed(mfaUsed)
                .build());
    }

    public void logFailure(String email, String ipAddress, String reason) {
        loginLogRepository.save(LoginLog.builder()
                .email(email)
                .ipAddress(ipAddress)
                .location(geoLocationService.getLocation(ipAddress))
                .success(false)
                .mfaUsed(false)
                .failureReason(reason)
                .build());
    }

    public List<LoginLogDto> getRecentLogs(User user, int limit) {
        return loginLogRepository
                .findByUserOrderByTimestampDesc(user, PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public long countFailedAttempts(User user) {
        return loginLogRepository.countFailedAttemptsByUser(user);
    }

    public List<LoginLog> getAllLogs(User user) {
        return loginLogRepository.findByUserOrderByTimestampDesc(user);
    }

    public Map<String, Long> getDailyLoginCounts(User user, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> rows = loginLogRepository.countLoginsByDay(user.getId(), since);
        Map<String, Long> result = new LinkedHashMap<>();
        rows.forEach(r -> result.put(String.valueOf(r[0]), ((Number) r[1]).longValue()));
        return result;
    }

    private LoginLogDto toDto(LoginLog log) {
        return LoginLogDto.builder()
                .id(log.getId())
                .ipAddress(log.getIpAddress())
                .deviceLabel(log.getDeviceLabel())
                .location(log.getLocation())
                .success(log.isSuccess())
                .mfaUsed(log.isMfaUsed())
                .timestamp(log.getTimestamp())
                .build();
    }
}
