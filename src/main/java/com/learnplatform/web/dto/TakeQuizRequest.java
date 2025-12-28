package com.learnplatform.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;

public record TakeQuizRequest(
        @NotNull @Positive Long studentId,
        Map<@NotNull @Positive Long, List<@NotNull @Positive Long>> answers
) {
}
