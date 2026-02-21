package com.chitchatclub.api.dto.response;

import java.util.List;
import java.util.UUID;

public record BreakoutRoomResponse(
        UUID id,
        String levelBucket,
        Integer roomIndex,
        List<UserResponse> moderators,
        List<UserResponse> members
) {}
