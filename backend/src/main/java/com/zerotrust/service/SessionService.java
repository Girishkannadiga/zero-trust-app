package com.zerotrust.service;

import com.zerotrust.dto.SessionDto;
import com.zerotrust.model.ActiveSession;
import com.zerotrust.model.User;
import com.zerotrust.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public String createSession(User user, String deviceLabel, String ipAddress,
                                String fingerprint, LocalDateTime expiresAt) {
        String sessionId = UUID.randomUUID().toString();
        sessionRepository.save(ActiveSession.builder()
                .id(sessionId)
                .user(user)
                .deviceLabel(deviceLabel)
                .ipAddress(ipAddress)
                .deviceFingerprint(fingerprint)
                .expiresAt(expiresAt)
                .revoked(false)
                .current(true)
                .build());
        return sessionId;
    }

    @Transactional
    public void revokeSession(String sessionId, User user) {
        sessionRepository.revokeById(sessionId, user);
    }

    @Transactional
    public void revokeAllExcept(String currentSessionId, User user) {
        sessionRepository.revokeAllExcept(user, currentSessionId);
    }

    public List<SessionDto> getActiveSessions(User user) {
        return sessionRepository.findByUserAndRevokedFalse(user)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public long countActiveSessions(User user) {
        return sessionRepository.countByUserAndRevokedFalse(user);
    }

    private SessionDto toDto(ActiveSession session) {
        return SessionDto.builder()
                .sessionId(session.getId())
                .deviceLabel(session.getDeviceLabel())
                .ipAddress(session.getIpAddress())
                .startedAt(session.getStartedAt())
                .current(session.isCurrent())
                .build();
    }
}
