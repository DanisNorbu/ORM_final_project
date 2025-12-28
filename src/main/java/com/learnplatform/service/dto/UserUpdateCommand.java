package com.learnplatform.service.dto;

import com.learnplatform.entity.UserRole;

public record UserUpdateCommand(
        String name,
        String email,
        UserRole role
) {
}
