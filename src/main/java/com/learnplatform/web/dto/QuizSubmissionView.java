package com.learnplatform.web.dto;

import java.time.OffsetDateTime;

public record QuizSubmissionView(
        Long id,
        Long quizId,
        Long studentId,
        int score,
        OffsetDateTime takenAt
) {
}
