package com.learnplatform.service.dto;

import com.learnplatform.entity.UserRole;

public record UserCreateCommand(
        String name,
        String email,
        UserRole role
) {
}
