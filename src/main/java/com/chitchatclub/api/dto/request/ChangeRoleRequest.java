package com.chitchatclub.api.dto.request;

import com.chitchatclub.api.entity.enums.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull Role role
) {}
