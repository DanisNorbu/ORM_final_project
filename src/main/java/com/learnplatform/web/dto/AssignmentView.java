package com.learnplatform.web.dto;

import java.time.LocalDate;

public record AssignmentView(
        Long id,
        Long lessonId,
        String title,
        String description,
        LocalDate dueDate,
        Integer maxScore
) {
}
