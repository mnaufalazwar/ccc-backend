package com.chitchatclub.api.dto.response;

public record EmailPreviewResponse(
        String subject,
        String body,
        int recipientCount
) {}
