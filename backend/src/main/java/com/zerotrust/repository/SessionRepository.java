package com.zerotrust.repository;

import com.zerotrust.model.ActiveSession;
import com.zerotrust.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<ActiveSession, String> {
    List<ActiveSession> findByUserAndRevokedFalse(User user);
    long countByUserAndRevokedFalse(User user);

    @Modifying
    @Query("UPDATE ActiveSession s SET s.revoked = true WHERE s.user = :user AND s.id != :currentSessionId")
    void revokeAllExcept(User user, String currentSessionId);

    @Modifying
    @Query("UPDATE ActiveSession s SET s.revoked = true WHERE s.id = :sessionId AND s.user = :user")
    void revokeById(String sessionId, User user);
}
