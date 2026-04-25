package com.zerotrust.controller;

import com.zerotrust.dto.ApiResponse;
import com.zerotrust.dto.LoginRequest;
import com.zerotrust.dto.LoginResponse;
import com.zerotrust.dto.OtpRequest;
import com.zerotrust.dto.OtpVerifyRequest;
import com.zerotrust.dto.RegisterRequest;
import com.zerotrust.dto.UserDto;
import com.zerotrust.service.AuthService;
import com.zerotrust.service.MfaService;
import com.zerotrust.service.UserService;
import com.zerotrust.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final MfaService mfaService;
    private final UserService userService;
    private final IpUtils ipUtils;

    public AuthController(AuthService authService, MfaService mfaService,
                          UserService userService, IpUtils ipUtils) {
        this.authService = authService;
        this.mfaService = mfaService;
        this.userService = userService;
        this.ipUtils = ipUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserDto user = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Account created successfully.", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = ipUtils.extractClientIp(httpRequest);
        LoginResponse response = authService.login(request, ip);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyMfa(
            @Valid @RequestBody OtpVerifyRequest request,
            HttpServletRequest httpRequest) {

        String ip = ipUtils.extractClientIp(httpRequest);
        LoginResponse response = authService.verifyMfa(request, ip);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful.", response));
    }

    @PostMapping("/mfa/resend")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody OtpRequest request) {

        var user = userService.getByEmail(request.getEmail());
        mfaService.sendOtp(user, request.getMethod());
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully.", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully.", null));
    }
}
