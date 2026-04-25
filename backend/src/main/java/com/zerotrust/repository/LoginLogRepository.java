package com.zerotrust.repository;

import com.zerotrust.model.LoginLog;
import com.zerotrust.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    List<LoginLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);
    List<LoginLog> findByUserOrderByTimestampDesc(User user);

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.user = :user AND l.success = false")
    long countFailedAttemptsByUser(User user);

    @Query(value = "SELECT DATE(l.timestamp) AS d, COUNT(l.id) AS cnt " +
                   "FROM login_logs l WHERE l.user_id = :userId AND l.timestamp >= :since " +
                   "GROUP BY DATE(l.timestamp) ORDER BY DATE(l.timestamp)", nativeQuery = true)
    List<Object[]> countLoginsByDay(@org.springframework.data.repository.query.Param("userId") Long userId,
                                    @org.springframework.data.repository.query.Param("since") LocalDateTime since);
}
