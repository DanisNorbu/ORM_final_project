package com.learnplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModuleCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull Integer orderIndex,
        @Size(max = 2000) String description
) {
}
