package com.chitchatclub.api.dto.request;

import com.chitchatclub.api.entity.enums.LevelBucket;

public record UpdateProficiencyOverrideRequest(
        LevelBucket proficiencyLevel
) {}
