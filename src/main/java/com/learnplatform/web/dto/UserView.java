package com.learnplatform.web.dto;

import com.learnplatform.entity.UserRole;

public record UserView(
        Long id,
        String name,
        String email,
        UserRole role
) {
}
