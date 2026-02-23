package com.chitchatclub.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateSessionRequest(
        @NotBlank String title,
        @Size(max = 500) String description,
        @NotNull Instant startDateTime,
        @NotNull @Min(15) Integer durationMinutes,
        @NotNull @Min(2) Integer maxParticipants
) {}
