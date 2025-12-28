package com.learnplatform.service.dto;

import com.learnplatform.entity.QuestionType;

import java.util.List;

public record QuizCreateCommand(
        Long moduleId,
        String title,
        Integer timeLimit,
        List<QuestionCreateCommand> questions
) {

    public record QuestionCreateCommand(
            String text,
            QuestionType type,
            List<AnswerOptionCreateCommand> options
    ) {
    }

    public record AnswerOptionCreateCommand(
            String text,
            boolean isCorrect
    ) {
    }
}
