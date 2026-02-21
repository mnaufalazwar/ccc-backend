package com.chitchatclub.api.dto.request;

import com.chitchatclub.api.entity.enums.EnglishLevelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateEnglishLevelRequest(
        @NotNull EnglishLevelType englishLevelType,
        @NotBlank String englishLevelValue,
        String reason
) {}
