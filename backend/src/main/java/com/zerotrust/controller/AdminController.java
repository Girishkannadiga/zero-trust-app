package com.zerotrust.controller;

import com.zerotrust.dto.AdminUserDto;
import com.zerotrust.dto.ApiResponse;
import com.zerotrust.dto.UserDto;
import com.zerotrust.model.DeviceInfo;
import com.zerotrust.model.User;
import com.zerotrust.repository.UserRepository;
import com.zerotrust.service.DeviceTrackingService;
import com.zerotrust.service.LoginLogService;
import com.zerotrust.service.SessionService;
import com.zerotrust.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final LoginLogService loginLogService;
    private final DeviceTrackingService deviceTrackingService;
    private final SessionService sessionService;

    public AdminController(UserRepository userRepository,
                           UserService userService,
                           LoginLogService loginLogService,
                           DeviceTrackingService deviceTrackingService,
                           SessionService sessionService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.loginLogService = loginLogService;
        this.deviceTrackingService = deviceTrackingService;
        this.sessionService = sessionService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserDto>>> getAllUsers() {
        List<AdminUserDto> users = userRepository.findAll().stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    private AdminUserDto toAdminDto(User user) {
        // Last login log
        var logs = loginLogService.getRecentLogs(user, 1);
        String lastIp = logs.isEmpty() ? "—" : logs.get(0).getIpAddress();
        var lastLoginTime = logs.isEmpty() ? null : logs.get(0).getTimestamp();

        // Most recent device
        List<DeviceInfo> devices = deviceTrackingService.getUserDevices(user);
        String lastDevice = devices.isEmpty() ? "—" : devices.get(0).getLabel();
        boolean deviceTrusted = !devices.isEmpty() && devices.get(0).isTrusted();

        // Active sessions
        long activeSessions = sessionService.countActiveSessions(user);

        return AdminUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .mfaEnabled(user.isMfaEnabled())
                .accountLocked(user.isAccountLocked())
                .failedAttempts(user.getFailedLoginAttempts())
                .lastLoginIp(lastIp)
                .lastLoginTime(lastLoginTime)
                .lastDevice(lastDevice)
                .deviceTrusted(deviceTrusted)
                .activeSessions(activeSessions)
                .build();
    }

    @PutMapping("/users/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setAccountLocked(true);
            userRepository.save(user);
        });
        return ResponseEntity.ok(ApiResponse.success("User account locked.", null));
    }

    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
        });
        return ResponseEntity.ok(ApiResponse.success("User account unlocked.", null));
    }

    @PutMapping("/users/{id}/enable-mfa")
    public ResponseEntity<ApiResponse<Void>> enableMfa(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setMfaEnabled(true);
            userRepository.save(user);
        });
        return ResponseEntity.ok(ApiResponse.success("MFA enabled for user.", null));
    }

    @PutMapping("/users/{id}/disable-mfa")
    public ResponseEntity<ApiResponse<Void>> disableMfa(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setMfaEnabled(false);
            userRepository.save(user);
        });
        return ResponseEntity.ok(ApiResponse.success("MFA disabled for user.", null));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted.", null));
    }
}
