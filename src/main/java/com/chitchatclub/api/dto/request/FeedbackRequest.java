package com.chitchatclub.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record FeedbackRequest(
        UUID toUserId,
        @Min(1) @Max(5) Integer rating,
        String text,
        Boolean anonymous
) {}
