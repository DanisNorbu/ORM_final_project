package com.learnplatform.web.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpsertRequest(
        @Size(max = 4000) String bio,
        @Size(max = 1000) String avatarUrl
) {
}
