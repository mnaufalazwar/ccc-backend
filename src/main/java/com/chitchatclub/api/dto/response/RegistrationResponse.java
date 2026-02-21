package com.chitchatclub.api.dto.response;

import com.chitchatclub.api.entity.Registration;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        UserResponse user,
        Boolean attended,
        boolean registeredAsModerator,
        LocalDateTime createdAt
) {
    public static RegistrationResponse fromEntity(Registration reg) {
        return new RegistrationResponse(
                reg.getId(),
                UserResponse.fromEntity(reg.getUser(), true),
                reg.getAttended(),
                reg.isRegisteredAsModerator(),
                reg.getCreatedAt()
        );
    }
}
