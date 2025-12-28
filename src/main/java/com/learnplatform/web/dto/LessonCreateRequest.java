package com.learnplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 8000) String content,
        @Size(max = 1000) String videoUrl
) {
}
