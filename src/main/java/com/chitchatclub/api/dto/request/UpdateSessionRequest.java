package com.chitchatclub.api.dto.request;

import com.chitchatclub.api.entity.enums.SessionStatus;

import java.time.Instant;

public record UpdateSessionRequest(
        String title,
        String description,
        Instant startDateTime,
        Integer durationMinutes,
        Integer maxParticipants,
        SessionStatus status,
        String zoomLink,
        String zoomMeetingId,
        String zoomPassword
) {}
