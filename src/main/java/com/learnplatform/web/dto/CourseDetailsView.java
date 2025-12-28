package com.learnplatform.web.dto;

import java.time.LocalDate;
import java.util.List;

public record CourseDetailsView(
        Long id,
        String title,
        String description,
        Long categoryId,
        Long teacherId,
        String duration,
        LocalDate startDate,
        List<TagView> tags,
        List<ModuleView> modules
) {
}
