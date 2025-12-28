package com.learnplatform.web.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AssignmentPatchRequest(
        @Size(max = 200) String title,
        @Size(max = 4000) String description,
        LocalDate dueDate,
        @PositiveOrZero Integer maxScore
) {
}
