package com.learnplatform.web.dto;

import com.learnplatform.entity.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizCreateRequest(
        @NotNull @Positive Long moduleId,
        @NotBlank @Size(max = 200) String title,
        @PositiveOrZero Integer timeLimit,
        List<@Valid QuestionCreateRequest> questions
) {

    public record QuestionCreateRequest(
            @NotBlank @Size(max = 2000) String text,
            @NotNull QuestionType type,
            List<@Valid AnswerOptionCreateRequest> options
    ) {
    }

    public record AnswerOptionCreateRequest(
            @NotBlank @Size(max = 1000) String text,
            boolean isCorrect
    ) {
    }
}
