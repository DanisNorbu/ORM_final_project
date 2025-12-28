package com.learnplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record CourseCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String description,
        @NotNull @Positive Long categoryId,
        @NotNull @Positive Long teacherId,
        @Size(max = 50) String duration,
        LocalDate startDate,
        Set<@NotBlank @Size(max = 60) String> tagNames
) {
}
