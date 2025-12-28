package com.learnplatform.web.dto;

import com.learnplatform.entity.EnrollmentStatus;

import java.time.LocalDate;

public record EnrollmentView(
        Long id,
        Long userId,
        Long courseId,
        LocalDate enrollDate,
        EnrollmentStatus status
) {
}
