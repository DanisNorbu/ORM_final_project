package com.learnplatform.web.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SubmissionGradeRequest(
        @PositiveOrZero Integer score,
        @Size(max = 4000) String feedback
) {
}
