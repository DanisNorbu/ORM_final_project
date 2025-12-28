package com.learnplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryPatchRequest(
        @NotBlank @Size(max = 120) String name
) {
}
