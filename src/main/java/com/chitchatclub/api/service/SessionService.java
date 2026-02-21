package com.chitchatclub.api.service;

import com.chitchatclub.api.dto.request.CreateSessionRequest;
import com.chitchatclub.api.dto.request.UpdateSessionRequest;
import com.chitchatclub.api.dto.response.RegistrationResponse;
import com.chitchatclub.api.dto.response.SessionResponse;
import com.chitchatclub.api.entity.Registration;
import com.chitchatclub.api.entity.Session;
import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.entity.enums.Role;
import com.chitchatclub.api.entity.enums.SessionStatus;
import com.chitchatclub.api.exception.BadRequestException;
import com.chitchatclub.api.exception.ConflictException;
import com.chitchatclub.api.exception.ResourceNotFoundException;
import com.chitchatclub.api.repository.AppConfigRepository;
import com.chitchatclub.api.repository.RegistrationRepository;
import com.chitchatclub.api.repository.SessionRepository;
import com.chitchatclub.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final AppConfigRepository appConfigRepository;

    public SessionService(SessionRepository sessionRepository,
                          RegistrationRepository registrationRepository,
                          UserRepository userRepository,
                          AppConfigRepository appConfigRepository) {
        this.sessionRepository = sessionRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.appConfigRepository = appConfigRepository;
    }

    public SessionResponse createSession(CreateSessionRequest request, User creator) {
        Session session = new Session();
        session.setTitle(request.title());
        session.setDescription(request.description());
        session.setStartDateTime(request.startDateTime());
        session.setDurationMinutes(request.durationMinutes());
        session.setMaxParticipants(request.maxParticipants());
        session.setStatus(SessionStatus.DRAFT);
        session.setCreatedBy(creator);
        session.setAttendanceCode(generateAttendanceCode());

        session = sessionRepository.save(session);
        return SessionResponse.fromEntity(session, 0);
    }

    public SessionResponse updateSession(UUID id, UpdateSessionRequest request) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        if (request.title() != null) session.setTitle(request.title());
        if (request.description() != null) session.setDescription(request.description());
        if (request.startDateTime() != null) session.setStartDateTime(request.startDateTime());
        if (request.durationMinutes() != null) session.setDurationMinutes(request.durationMinutes());
        if (request.maxParticipants() != null) session.setMaxParticipants(request.maxParticipants());
        if (request.status() != null) session.setStatus(request.status());
        if (request.zoomLink() != null) session.setZoomLink(request.zoomLink());
        if (request.zoomMeetingId() != null) session.setZoomMeetingId(request.zoomMeetingId());
        if (request.zoomPassword() != null) session.setZoomPassword(request.zoomPassword());

        session = sessionRepository.save(session);
        long count = registrationRepository.countBySessionId(session.getId());
        return SessionResponse.fromEntity(session, count);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getOpenSessions() {
        return sessionRepository.findByStatus(SessionStatus.OPEN).stream()
                .map(s -> SessionResponse.fromEntity(s, registrationRepository.countBySessionId(s.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getUpcomingSessions() {
        return sessionRepository.findByStatusAndStartDateTimeAfterOrderByStartDateTimeAsc(
                        SessionStatus.OPEN, LocalDateTime.now())
                .stream()
                .limit(3)
                .map(s -> SessionResponse.fromEntity(s, registrationRepository.countBySessionId(s.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getAllSessionsForAdmin() {
        return sessionRepository.findAll().stream()
                .map(s -> SessionResponse.fromEntity(s, registrationRepository.countBySessionId(s.getId()), true))
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(UUID id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        long count = registrationRepository.countBySessionId(session.getId());
        return SessionResponse.fromEntity(session, count);
    }

    @Transactional(readOnly = true)
    public Session getSessionEntity(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    public RegistrationResponse registerUser(UUID sessionId, User user, boolean asModerator) {
        Session session = getSessionEntity(sessionId);

        if (session.getStatus() != SessionStatus.OPEN) {
            throw new BadRequestException("Session is not open for registration");
        }

        if (asModerator) {
            Role role = user.getRole();
            if (role != Role.MODERATOR && role != Role.ADMIN && role != Role.SUPER_ADMIN) {
                throw new BadRequestException("Only users with moderator privileges can register as a moderator");
            }
        }

        if (user.getBlacklistedUntil() != null) {
            if (user.getBlacklistedUntil().isAfter(LocalDateTime.now())) {
                throw new BadRequestException("You are currently blacklisted until " + user.getBlacklistedUntil().toLocalDate() + " due to repeated no-shows");
            }
            user.setBlacklistedUntil(null);
            user.setNoShowCount(0);
            userRepository.save(user);
        }

        long currentCount = registrationRepository.countBySessionId(sessionId);
        if (currentCount >= session.getMaxParticipants()) {
            throw new BadRequestException("Session is full");
        }

        if (registrationRepository.existsBySessionIdAndUserId(sessionId, user.getId())) {
            throw new ConflictException("User already registered for this session");
        }

        Registration registration = new Registration();
        registration.setSession(session);
        registration.setUser(user);
        registration.setRegisteredAsModerator(asModerator);
        registration = registrationRepository.save(registration);

        return RegistrationResponse.fromEntity(registration);
    }

    public void unregisterUser(UUID sessionId, User user) {
        Registration registration = registrationRepository
                .findBySessionIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        Session session = registration.getSession();
        int cutoffHours = appConfigRepository.findById("unregister_cutoff_hours")
                .map(c -> { try { return Integer.parseInt(c.getConfigValue()); } catch (NumberFormatException e) { return 24; } })
                .orElse(24);
        if (session.getStartDateTime() != null
                && session.getStartDateTime().minusHours(cutoffHours).isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "You cannot unregister within " + cutoffHours + " hours of the session start time.");
        }

        registrationRepository.delete(registration);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrations(UUID sessionId) {
        return registrationRepository.findBySessionId(sessionId).stream()
                .map(RegistrationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionByIdForAdmin(UUID id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        long count = registrationRepository.countBySessionId(session.getId());
        return SessionResponse.fromEntity(session, count, true);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getMyRegisteredSessions(UUID userId) {
        return registrationRepository.findByUserId(userId).stream()
                .map(reg -> {
                    Session session = reg.getSession();
                    long count = registrationRepository.countBySessionId(session.getId());
                    return SessionResponse.fromEntity(session, count);
                })
                .toList();
    }

    private String generateAttendanceCode() {
        return String.format("%02d", new java.util.Random().nextInt(100));
    }
}
