package com.learnplatform.service.dto;

import java.time.LocalDate;
import java.util.Set;

public record CourseCreateCommand(
        String title,
        String description,
        Long categoryId,
        Long teacherId,
        String duration,
        LocalDate startDate,
        Set<String> tagNames
) {
}
