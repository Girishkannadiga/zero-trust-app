package com.zerotrust.repository;

import com.zerotrust.model.DeviceInfo;
import com.zerotrust.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceInfo, Long> {
    Optional<DeviceInfo> findByUserAndFingerprint(User user, String fingerprint);
    List<DeviceInfo> findByUser(User user);
    List<DeviceInfo> findByUserAndTrusted(User user, boolean trusted);
    long countByUserAndTrusted(User user, boolean trusted);
}
