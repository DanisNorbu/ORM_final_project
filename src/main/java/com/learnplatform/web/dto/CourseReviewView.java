package com.learnplatform.web.dto;

import java.time.OffsetDateTime;

public record CourseReviewView(
        Long id,
        Long courseId,
        Long studentId,
        int rating,
        String comment,
        OffsetDateTime createdAt
) {
}
