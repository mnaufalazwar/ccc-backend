package com.chitchatclub.api.dto.request;

import com.chitchatclub.api.entity.enums.EnglishLevelType;
import jakarta.validation.constraints.NotNull;

public record UpdateMyEnglishLevelRequest(
        @NotNull EnglishLevelType englishLevelType,
        String englishLevelValue
) {}
