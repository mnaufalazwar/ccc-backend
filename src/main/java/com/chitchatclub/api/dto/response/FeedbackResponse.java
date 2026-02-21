package com.chitchatclub.api.dto.response;

import com.chitchatclub.api.entity.Feedback;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedbackResponse(
        UUID id,
        UserResponse fromUser,
        UserResponse toUser,
        Integer rating,
        String text,
        boolean anonymous,
        LocalDateTime createdAt
) {
    public static FeedbackResponse fromEntity(Feedback f) {
        return new FeedbackResponse(
                f.getId(),
                UserResponse.fromEntity(f.getFromUser(), false),
                f.getToUser() != null ? UserResponse.fromEntity(f.getToUser(), false) : null,
                f.getRating(),
                f.getText(),
                f.isAnonymous(),
                f.getCreatedAt()
        );
    }

    public static FeedbackResponse fromEntityHideAuthor(Feedback f) {
        return new FeedbackResponse(
                f.getId(),
                f.isAnonymous() ? null : UserResponse.fromEntity(f.getFromUser(), false),
                f.getToUser() != null ? UserResponse.fromEntity(f.getToUser(), false) : null,
                f.getRating(),
                f.getText(),
                f.isAnonymous(),
                f.getCreatedAt()
        );
    }
}
