package com.chitchatclub.api.dto.request;

import com.chitchatclub.api.entity.enums.SessionStatus;

import java.time.LocalDateTime;

public record UpdateSessionRequest(
        String title,
        String description,
        LocalDateTime startDateTime,
        Integer durationMinutes,
        Integer maxParticipants,
        SessionStatus status,
        String zoomLink,
        String zoomMeetingId,
        String zoomPassword
) {}
