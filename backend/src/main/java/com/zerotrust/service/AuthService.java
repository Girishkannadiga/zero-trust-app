package com.zerotrust.service;

import com.zerotrust.dto.LoginRequest;
import com.zerotrust.dto.LoginResponse;
import com.zerotrust.dto.OtpVerifyRequest;
import com.zerotrust.model.User;
import com.zerotrust.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final MfaService mfaService;
    private final DeviceTrackingService deviceTrackingService;
    private final IpTrackingService ipTrackingService;
    private final LoginLogService loginLogService;
    private final SessionService sessionService;
    private final MailService mailService;
    private final GeoLocationService geoLocationService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       UserService userService,
                       MfaService mfaService,
                       DeviceTrackingService deviceTrackingService,
                       IpTrackingService ipTrackingService,
                       LoginLogService loginLogService,
                       SessionService sessionService,
                       MailService mailService,
                       GeoLocationService geoLocationService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.mfaService = mfaService;
        this.deviceTrackingService = deviceTrackingService;
        this.ipTrackingService = ipTrackingService;
        this.loginLogService = loginLogService;
        this.sessionService = sessionService;
        this.mailService = mailService;
        this.geoLocationService = geoLocationService;
    }

    public LoginResponse login(LoginRequest request, String ipAddress) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (LockedException e) {
            loginLogService.logFailure(request.getEmail(), ipAddress, "Account locked");
            throw new LockedException("ACCOUNT_LOCKED");
        } catch (BadCredentialsException e) {
            loginLogService.logFailure(request.getEmail(), ipAddress, "Bad credentials");
            throw new BadCredentialsException("Invalid email or password.");
        }

        User user = userService.getByEmail(request.getEmail());
        userService.resetFailedAttempts(user);

        deviceTrackingService.trackDevice(
                user,
                request.getDeviceFingerprint(),
                request.getDeviceLabel(),
                request.getDeviceOs(),
                request.getDeviceBrowser()
        );
        ipTrackingService.trackIp(user, ipAddress);

        mfaService.sendOtp(user, "EMAIL");
        return LoginResponse.builder()
                .mfaRequired(true)
                .message("OTP sent to your registered email address.")
                .build();
    }

    public LoginResponse verifyMfa(OtpVerifyRequest request, String ipAddress) {
        mfaService.verifyOtp(request.getEmail(), request.getOtp(), request.getMethod());

        User user = userService.getByEmail(request.getEmail());

        if (request.isRememberDevice()) {
            deviceTrackingService.trustDevice(user, request.getDeviceFingerprint());
        }

        return issueToken(user, ipAddress, request.getDeviceFingerprint(), null, true);
    }

    private LoginResponse issueToken(User user, String ipAddress,
                                     String fingerprint, String deviceLabel,
                                     boolean mfaUsed) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtExpiration / 1000);
        String sessionId = sessionService.createSession(
                user, deviceLabel, ipAddress, fingerprint, expiresAt);
        String token = jwtTokenProvider.generateToken(
                user.getEmail(), user.getRole().name(), sessionId, ipAddress);

        String resolvedDevice = deviceLabel;
        if (resolvedDevice == null && fingerprint != null) {
            resolvedDevice = deviceTrackingService.getUserDevices(user).stream()
                    .filter(d -> d.getFingerprint().equals(fingerprint))
                    .findFirst().map(d -> d.getLabel()).orElse("Unknown Device");
        }

        loginLogService.logSuccess(user, ipAddress, resolvedDevice, mfaUsed);

        String location = geoLocationService.getLocation(ipAddress);
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        mailService.sendLoginNotificationEmail(user.getEmail(), user.getName(), ipAddress, location, resolvedDevice, time);

        return LoginResponse.builder()
                .token(token)
                .mfaRequired(false)
                .user(userService.toDto(user))
                .build();
    }
}
