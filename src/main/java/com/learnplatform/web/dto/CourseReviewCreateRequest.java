package com.learnplatform.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CourseReviewCreateRequest(
        @NotNull @Positive Long studentId,
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 2000) String comment
) {
}
