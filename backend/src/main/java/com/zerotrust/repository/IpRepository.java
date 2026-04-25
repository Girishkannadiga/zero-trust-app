package com.zerotrust.repository;

import com.zerotrust.model.IpRecord;
import com.zerotrust.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IpRepository extends JpaRepository<IpRecord, Long> {
    Optional<IpRecord> findByUserAndIpAddress(User user, String ipAddress);
    List<IpRecord> findByUser(User user);

    @Query("SELECT COUNT(DISTINCT r.ipAddress) FROM IpRecord r WHERE r.user = :user AND r.firstSeen >= :since")
    long countDistinctIpsSince(User user, LocalDateTime since);
}
