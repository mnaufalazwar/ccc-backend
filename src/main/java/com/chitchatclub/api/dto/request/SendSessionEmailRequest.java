package com.chitchatclub.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SendSessionEmailRequest(
        @NotBlank String subject,
        @NotBlank String body
) {}
