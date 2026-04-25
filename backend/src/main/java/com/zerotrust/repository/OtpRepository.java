package com.zerotrust.repository;

import com.zerotrust.model.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpRecord, Long> {

    Optional<OtpRecord> findTopByEmailAndMethodAndUsedFalseOrderByCreatedAtDesc(
            String email, String method);

    @Modifying
    @Query("UPDATE OtpRecord o SET o.used = true WHERE o.email = :email AND o.method = :method")
    void invalidateAllForEmail(String email, String method);
}
