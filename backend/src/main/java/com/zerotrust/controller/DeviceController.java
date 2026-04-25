package com.zerotrust.controller;

import com.zerotrust.dto.ApiResponse;
import com.zerotrust.model.DeviceInfo;
import com.zerotrust.service.DeviceTrackingService;
import com.zerotrust.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceTrackingService deviceTrackingService;
    private final UserService userService;

    public DeviceController(DeviceTrackingService deviceTrackingService, UserService userService) {
        this.deviceTrackingService = deviceTrackingService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceInfo>>> getDevices(
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(deviceTrackingService.getUserDevices(user)));
    }

    @PutMapping("/{id}/trust")
    public ResponseEntity<ApiResponse<Void>> trustDevice(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        deviceTrackingService.getUserDevices(user).stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .ifPresent(d -> deviceTrackingService.trustDevice(user, d.getFingerprint()));

        return ResponseEntity.ok(ApiResponse.success("Device marked as trusted.", null));
    }

    @PutMapping("/{id}/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeDevice(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        var user = userService.getByEmail(principal.getUsername());
        deviceTrackingService.revokeDevice(user, id);
        return ResponseEntity.ok(ApiResponse.success("Device trust revoked.", null));
    }
}
