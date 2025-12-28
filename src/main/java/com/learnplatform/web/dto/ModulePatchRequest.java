package com.learnplatform.web.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ModulePatchRequest(
        @Size(max = 200) String title,
        @Positive Integer orderIndex,
        @Size(max = 4000) String description
) {
}
