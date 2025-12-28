package com.learnplatform.web.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record CoursePatchRequest(
        @Size(max = 200) String title,
        @Size(max = 4000) String description,
        @Positive Long categoryId,
        @Positive Long teacherId,
        @Size(max = 50) String duration,
        LocalDate startDate,
        Set<@Size(max = 60) String> tagNames
) {
}
