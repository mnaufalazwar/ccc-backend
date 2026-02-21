package com.chitchatclub.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MoveRoomMemberRequest(
        @NotNull UUID targetRoomId
) {}
