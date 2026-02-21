package com.chitchatclub.api.dto.response;

import com.chitchatclub.api.entity.Session;
import com.chitchatclub.api.entity.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String title,
        String description,
        LocalDateTime startDateTime,
        Integer durationMinutes,
        Integer maxParticipants,
        SessionStatus status,
        Long currentRegistrations,
        UserResponse createdBy,
        String attendanceCode,
        String zoomLink,
        String zoomMeetingId,
        String zoomPassword,
        LocalDateTime createdAt
) {
    public static SessionResponse fromEntity(Session session, long regCount) {
        return fromEntity(session, regCount, false);
    }

    public static SessionResponse fromEntity(Session session, long regCount, boolean includeAttendanceCode) {
        return new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getStartDateTime(),
                session.getDurationMinutes(),
                session.getMaxParticipants(),
                session.getStatus(),
                regCount,
                UserResponse.fromEntity(session.getCreatedBy(), true),
                includeAttendanceCode ? session.getAttendanceCode() : null,
                session.getZoomLink(),
                session.getZoomMeetingId(),
                session.getZoomPassword(),
                session.getCreatedAt()
        );
    }
}
