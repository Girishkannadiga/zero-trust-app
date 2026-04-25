package com.zerotrust.controller;

import com.zerotrust.dto.*;
import com.zerotrust.model.LoginLog;
import com.zerotrust.security.jwt.JwtTokenProvider;
import com.zerotrust.service.DashboardService;
import com.zerotrust.service.LoginLogService;
import com.zerotrust.service.SessionService;
import com.zerotrust.service.UserService;
import com.zerotrust.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final LoginLogService loginLogService;
    private final SessionService sessionService;
    private final UserService userService;
    private final IpUtils ipUtils;
    private final JwtTokenProvider jwtTokenProvider;

    public DashboardController(DashboardService dashboardService, LoginLogService loginLogService,
                               SessionService sessionService, UserService userService,
                               IpUtils ipUtils, JwtTokenProvider jwtTokenProvider) {
        this.dashboardService = dashboardService;
        this.loginLogService = loginLogService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.ipUtils = ipUtils;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/security-status")
    public ResponseEntity<ApiResponse<SecurityStatusDto>> getSecurityStatus(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false, defaultValue = "") String fingerprint,
            HttpServletRequest request) {

        String ip = ipUtils.extractClientIp(request);
        SecurityStatusDto status = dashboardService.getSecurityStatus(
                principal.getUsername(), ip, fingerprint);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats(
            @AuthenticationPrincipal UserDetails principal) {

        DashboardStatsDto stats = dashboardService.getStats(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/risk-score")
    public ResponseEntity<ApiResponse<RiskScoreDto>> getRiskScore(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false, defaultValue = "") String fingerprint,
            HttpServletRequest request) {

        String ip = ipUtils.extractClientIp(request);
        RiskScoreDto riskScore = dashboardService.getRiskScore(
                principal.getUsername(), ip, fingerprint);
        return ResponseEntity.ok(ApiResponse.success(riskScore));
    }

    @GetMapping("/login-logs")
    public ResponseEntity<ApiResponse<List<LoginLogDto>>> getLoginLogs(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "10") int limit) {

        var user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(loginLogService.getRecentLogs(user, limit)));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<SessionDto>>> getActiveSessions(
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(sessionService.getActiveSessions(user)));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        sessionService.revokeSession(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success("Session revoked.", null));
    }

    @PostMapping("/sessions/revoke-all")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessions(
            @AuthenticationPrincipal UserDetails principal,
            HttpServletRequest httpRequest) {

        var user = userService.getByEmail(principal.getUsername());
        String authHeader = httpRequest.getHeader("Authorization");
        String currentSessionId = "";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            currentSessionId = jwtTokenProvider.extractSessionId(authHeader.substring(7));
        }
        sessionService.revokeAllExcept(currentSessionId, user);
        return ResponseEntity.ok(ApiResponse.success("All other sessions revoked.", null));
    }

    @GetMapping("/login-logs/export")
    public ResponseEntity<byte[]> exportLoginLogsCsv(
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        List<LoginLog> logs = loginLogService.getAllLogs(user);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder csv = new StringBuilder("Timestamp,IP Address,Device,Location,Status,MFA Used\n");
        for (LoginLog log : logs) {
            csv.append(escape(log.getTimestamp() != null ? log.getTimestamp().format(fmt) : "")).append(',')
               .append(escape(log.getIpAddress())).append(',')
               .append(escape(log.getDeviceLabel())).append(',')
               .append(escape(log.getLocation())).append(',')
               .append(log.isSuccess() ? "Success" : "Failed").append(',')
               .append(log.isMfaUsed() ? "Yes" : "No").append('\n');
        }

        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"login-logs.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    private String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    @GetMapping("/chart-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChartData(
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        Map<String, Long> dailyCounts = loginLogService.getDailyLoginCounts(user, 7);
        return ResponseEntity.ok(ApiResponse.success(Map.of("dailyLogins", dailyCounts)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserDto updated = userService.updateName(principal.getUsername(), request.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile updated.", updated));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(principal.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully.", null));
    }
}
