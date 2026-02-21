package com.chitchatclub.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyAttendanceRequest(@NotBlank String attendanceCode) {}
