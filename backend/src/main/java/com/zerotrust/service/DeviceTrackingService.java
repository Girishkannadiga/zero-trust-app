package com.zerotrust.service;

import com.zerotrust.model.DeviceInfo;
import com.zerotrust.model.User;
import com.zerotrust.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceTrackingService {

    private final DeviceRepository deviceRepository;

    public DeviceTrackingService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceInfo trackDevice(User user, String fingerprint,
                                  String label, String os, String browser) {
        return deviceRepository.findByUserAndFingerprint(user, fingerprint)
                .map(device -> {
                    device.setLabel(label);
                    device.setOs(os);
                    device.setBrowser(browser);
                    return deviceRepository.save(device);
                })
                .orElseGet(() -> deviceRepository.save(DeviceInfo.builder()
                        .user(user)
                        .fingerprint(fingerprint)
                        .label(label)
                        .os(os)
                        .browser(browser)
                        .trusted(false)
                        .build()));
    }

    public boolean isTrusted(User user, String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) return false;
        return deviceRepository.findByUserAndFingerprint(user, fingerprint)
                .map(DeviceInfo::isTrusted)
                .orElse(false);
    }

    public void trustDevice(User user, String fingerprint) {
        deviceRepository.findByUserAndFingerprint(user, fingerprint).ifPresent(device -> {
            device.setTrusted(true);
            deviceRepository.save(device);
        });
    }

    public void revokeDevice(User user, Long deviceId) {
        deviceRepository.findById(deviceId).ifPresent(device -> {
            if (device.getUser().getId().equals(user.getId())) {
                device.setTrusted(false);
                deviceRepository.save(device);
            }
        });
    }

    public List<DeviceInfo> getUserDevices(User user) {
        return deviceRepository.findByUser(user);
    }

    public long countTrustedDevices(User user) {
        return deviceRepository.countByUserAndTrusted(user, true);
    }

    public long countUntrustedDevices(User user) {
        return deviceRepository.countByUserAndTrusted(user, false);
    }
}
