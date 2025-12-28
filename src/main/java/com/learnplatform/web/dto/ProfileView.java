package com.learnplatform.web.dto;

public record ProfileView(
        Long id,
        Long userId,
        String bio,
        String avatarUrl
) {
}
