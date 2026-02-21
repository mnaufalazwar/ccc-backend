package com.chitchatclub.api.dto.response;

import com.chitchatclub.api.entity.EnglishLevelHistory;
import com.chitchatclub.api.entity.enums.EnglishLevelType;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnglishLevelHistoryResponse(
        UUID id,
        EnglishLevelType previousLevelType,
        String previousLevelValue,
        EnglishLevelType newLevelType,
        String newLevelValue,
        UserResponse changedBy,
        String reason,
        LocalDateTime createdAt
) {
    public static EnglishLevelHistoryResponse fromEntity(EnglishLevelHistory h) {
        return new EnglishLevelHistoryResponse(
                h.getId(),
                h.getPreviousLevelType(),
                h.getPreviousLevelValue(),
                h.getNewLevelType(),
                h.getNewLevelValue(),
                UserResponse.fromEntity(h.getChangedBy(), true),
                h.getReason(),
                h.getCreatedAt()
        );
    }
}
