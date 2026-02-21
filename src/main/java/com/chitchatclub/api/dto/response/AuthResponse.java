package com.chitchatclub.api.dto.response;

public record AuthResponse(
        String token,
        String refreshToken,
        UserResponse user
) {}
