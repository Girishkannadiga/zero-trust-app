package com.zerotrust.service;

import com.zerotrust.model.IpRecord;
import com.zerotrust.model.User;
import com.zerotrust.repository.IpRepository;
import com.zerotrust.util.IpUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IpTrackingService {

    private final IpRepository ipRepository;
    private final IpUtils ipUtils;

    public IpTrackingService(IpRepository ipRepository, IpUtils ipUtils) {
        this.ipRepository = ipRepository;
        this.ipUtils = ipUtils;
    }

    public IpRecord trackIp(User user, String ipAddress) {
        return ipRepository.findByUserAndIpAddress(user, ipAddress)
                .map(record -> {
                    record.setAccessCount(record.getAccessCount() + 1);
                    return ipRepository.save(record);
                })
                .orElseGet(() -> ipRepository.save(IpRecord.builder()
                        .user(user)
                        .ipAddress(ipAddress)
                        .trusted(ipUtils.isPrivateIp(ipAddress))
                        .suspicious(false)
                        .accessCount(1)
                        .build()));
    }

    public boolean isNewIp(User user, String ipAddress) {
        return ipRepository.findByUserAndIpAddress(user, ipAddress)
                .map(r -> r.getAccessCount() == 1)
                .orElse(false);
    }

    public boolean isSuspicious(User user, String ipAddress) {
        if (ipAddress == null) return false;
        return ipRepository.findByUserAndIpAddress(user, ipAddress)
                .map(IpRecord::isSuspicious)
                .orElse(false);
    }

    public void markSuspicious(User user, String ipAddress) {
        ipRepository.findByUserAndIpAddress(user, ipAddress).ifPresent(record -> {
            record.setSuspicious(true);
            ipRepository.save(record);
        });
    }

    public long countUniqueIpsLast30Days(User user) {
        return ipRepository.countDistinctIpsSince(user, LocalDateTime.now().minusDays(30));
    }

    public List<IpRecord> getUserIpRecords(User user) {
        return ipRepository.findByUser(user);
    }
}
