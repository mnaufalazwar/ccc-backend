package com.chitchatclub.api.service;

import com.chitchatclub.api.entity.*;
import com.chitchatclub.api.entity.enums.SessionStatus;
import com.chitchatclub.api.exception.*;
import com.chitchatclub.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AttendanceService {

    private final SessionRepository sessionRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final AppConfigRepository appConfigRepository;

    public AttendanceService(SessionRepository sessionRepository,
                             RegistrationRepository registrationRepository,
                             UserRepository userRepository,
                             AppConfigRepository appConfigRepository) {
        this.sessionRepository = sessionRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.appConfigRepository = appConfigRepository;
    }

    public boolean verifyAttendance(UUID sessionId, User user, String code) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new BadRequestException("Session is not yet completed");
        }
        Registration reg = registrationRepository.findBySessionIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new BadRequestException("You are not registered for this session"));
        if (reg.getAttended() != null && reg.getAttended()) {
            throw new BadRequestException("Attendance already verified");
        }
        boolean matches = session.getAttendanceCode().equals(code.trim());
        if (matches) {
            reg.setAttended(true);
            registrationRepository.save(reg);
        }
        return matches;
    }

    public void finalizeAttendance(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new BadRequestException("Session must be COMPLETED to finalize attendance");
        }
        List<Registration> unverified = registrationRepository.findBySessionIdAndAttendedIsNull(sessionId);
        int maxNoShows = getConfigInt("max_no_shows", 3);
        int blacklistDays = getConfigInt("blacklist_duration_days", 30);

        for (Registration reg : unverified) {
            reg.setAttended(false);
            registrationRepository.save(reg);

            User u = reg.getUser();
            u.setNoShowCount(u.getNoShowCount() + 1);
            if (u.getNoShowCount() >= maxNoShows) {
                u.setBlacklistedUntil(LocalDateTime.now().plusDays(blacklistDays));
            }
            userRepository.save(u);
        }
    }

    public int getConfigInt(String key, int defaultValue) {
        return appConfigRepository.findById(key)
                .map(c -> {
                    try { return Integer.parseInt(c.getConfigValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                })
                .orElse(defaultValue);
    }

    public void setConfig(String key, String value) {
        AppConfig config = appConfigRepository.findById(key).orElse(new AppConfig());
        config.setConfigKey(key);
        config.setConfigValue(value);
        appConfigRepository.save(config);
    }

    public void whitelistUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setBlacklistedUntil(null);
        user.setNoShowCount(0);
        userRepository.save(user);
    }
}
