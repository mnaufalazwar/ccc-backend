package com.chitchatclub.api.dto.request;

import com.chitchatclub.api.entity.enums.EnglishLevelType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        EnglishLevelType englishLevelType,
        String englishLevelValue
) {}
