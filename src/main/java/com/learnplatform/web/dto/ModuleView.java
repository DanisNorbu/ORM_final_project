package com.learnplatform.web.dto;

import java.util.List;

public record ModuleView(
        Long id,
        Long courseId,
        String title,
        int orderIndex,
        String description,
        List<LessonView> lessons
) {
}
