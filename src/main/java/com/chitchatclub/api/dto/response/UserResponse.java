package com.chitchatclub.api.dto.response;

import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.entity.enums.EnglishLevelType;
import com.chitchatclub.api.entity.enums.LevelBucket;
import com.chitchatclub.api.entity.enums.Role;
import com.chitchatclub.api.util.EnglishLevelNormalizer;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        Role role,
        EnglishLevelType englishLevelType,
        String englishLevelValue,
        String levelBucket,
        String proficiencyLevel,
        LevelBucket proficiencyLevelOverride,
        Integer noShowCount,
        LocalDateTime blacklistedUntil,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        return fromEntity(user, false);
    }

    public static UserResponse fromEntity(User user, boolean includeAdminFields) {
        return fromEntity(user, includeAdminFields, false);
    }

    public static UserResponse fromEntity(User user, boolean includeAdminFields, boolean isSelf) {
        String levelBucket = null;
        String proficiencyLevel = null;
        LevelBucket override = null;
        Integer noShowCount = null;
        LocalDateTime blacklistedUntil = null;

        if (includeAdminFields) {
            var calculatedBucket = EnglishLevelNormalizer.normalize(user.getEnglishLevelType(), user.getEnglishLevelValue());
            var effectiveBucket = EnglishLevelNormalizer.getEffectiveLevel(user);
            levelBucket = calculatedBucket.name();
            proficiencyLevel = effectiveBucket.getProficiencyLevel();
            override = user.getProficiencyLevelOverride();
        }

        if (includeAdminFields || isSelf) {
            noShowCount = user.getNoShowCount();
            blacklistedUntil = user.getBlacklistedUntil();
        }

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getEnglishLevelType(),
                user.getEnglishLevelValue(),
                levelBucket,
                proficiencyLevel,
                override,
                noShowCount,
                blacklistedUntil,
                user.getCreatedAt()
        );
    }
}
