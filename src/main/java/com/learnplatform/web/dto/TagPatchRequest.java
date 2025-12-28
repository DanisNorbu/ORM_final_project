package com.learnplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagPatchRequest(
        @NotBlank @Size(max = 64) String name
) {
}
