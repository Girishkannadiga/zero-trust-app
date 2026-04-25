package com.zerotrust.controller;

import com.zerotrust.dto.ApiResponse;
import com.zerotrust.dto.OtpRequest;
import com.zerotrust.service.MfaService;
import com.zerotrust.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final MfaService mfaService;
    private final UserService userService;

    public OtpController(MfaService mfaService, UserService userService) {
        this.mfaService = mfaService;
        this.userService = userService;
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody OtpRequest request) {

        var user = userService.getByEmail(request.getEmail());
        mfaService.sendOtp(user, request.getMethod());
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully.", null));
    }
}
