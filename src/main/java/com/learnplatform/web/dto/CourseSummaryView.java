package com.learnplatform.web.dto;

import java.time.LocalDate;

public record CourseSummaryView(
        Long id,
        String title,
        Long categoryId,
        Long teacherId,
        String duration,
        LocalDate startDate
) {
}
