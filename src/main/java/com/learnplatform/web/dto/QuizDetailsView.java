package com.learnplatform.web.dto;

import com.learnplatform.entity.QuestionType;

import java.util.List;

public record QuizDetailsView(
        Long id,
        Long moduleId,
        String title,
        Integer timeLimit,
        List<QuestionView> questions
) {

    public record QuestionView(
            Long id,
            String text,
            QuestionType type,
            List<AnswerOptionView> options
    ) {
    }

    public record AnswerOptionView(Long id, String text, boolean isCorrect) {
    }
}
