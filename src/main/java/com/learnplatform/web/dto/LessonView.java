package com.learnplatform.web.dto;

public record LessonView(
        Long id,
        Long moduleId,
        String title,
        String content,
        String videoUrl
) {
}
