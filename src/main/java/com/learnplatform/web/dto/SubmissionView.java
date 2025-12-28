package com.learnplatform.web.dto;

import java.time.OffsetDateTime;

public record SubmissionView(
        Long id,
        Long assignmentId,
        Long studentId,
        OffsetDateTime submittedAt,
        String content,
        Integer score,
        String feedback
) {
}
