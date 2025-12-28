package com.learnplatform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.learnplatform.entity.AnswerOption;
import com.learnplatform.entity.Question;
import com.learnplatform.entity.QuestionType;
import com.learnplatform.entity.Quiz;
import com.learnplatform.entity.QuizSubmission;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.BadRequestException;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.repository.ModuleRepository;
import com.learnplatform.repository.QuizRepository;
import com.learnplatform.repository.QuizSubmissionRepository;
import com.learnplatform.repository.UserRepository;
import com.learnplatform.testutil.EntityIdSetter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    QuizRepository quizRepository;
    @Mock
    QuizSubmissionRepository quizSubmissionRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ModuleRepository moduleRepository;

    @InjectMocks
    QuizService quizService;

    @Test
    void takeQuiz_calculatesScore() {
        Quiz quiz = new Quiz("Quiz", 10);
        EntityIdSetter.setId(quiz, 100L);

        Question q1 = new Question("q1", QuestionType.SINGLE_CHOICE);
        EntityIdSetter.setId(q1, 1L);
        AnswerOption q1a = new AnswerOption("a", true);
        EntityIdSetter.setId(q1a, 11L);
        AnswerOption q1b = new AnswerOption("b", false);
        EntityIdSetter.setId(q1b, 12L);
        q1.addOption(q1a);
        q1.addOption(q1b);
        quiz.addQuestion(q1);

        Question q2 = new Question("q2", QuestionType.MULTIPLE_CHOICE);
        EntityIdSetter.setId(q2, 2L);
        AnswerOption q2a = new AnswerOption("a", true);
        EntityIdSetter.setId(q2a, 21L);
        AnswerOption q2b = new AnswerOption("b", true);
        EntityIdSetter.setId(q2b, 22L);
        AnswerOption q2c = new AnswerOption("c", false);
        EntityIdSetter.setId(q2c, 23L);
        q2.addOption(q2a);
        q2.addOption(q2b);
        q2.addOption(q2c);
        quiz.addQuestion(q2);

        when(quizRepository.findWithQuestionsById(100L)).thenReturn(Optional.of(quiz));
        User student = new User("S", "s@x", UserRole.STUDENT);
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(quizSubmissionRepository.save(any(QuizSubmission.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizSubmission result = quizService.takeQuiz(10L, 100L, Map.of(
                1L, List.of(11L),
                2L, List.of(21L, 22L)
        ));

        assertEquals(2, result.getScore());
        assertNotNull(result.getTakenAt());
        assertEquals(student, result.getStudent());
        assertEquals(quiz, result.getQuiz());
    }

    @Test
    void takeQuiz_singleChoiceMultipleAnswers_throwsBadRequest() {
        Quiz quiz = new Quiz("Quiz", 10);
        EntityIdSetter.setId(quiz, 100L);
        Question q1 = new Question("q1", QuestionType.SINGLE_CHOICE);
        EntityIdSetter.setId(q1, 1L);
        AnswerOption q1a = new AnswerOption("a", true);
        EntityIdSetter.setId(q1a, 11L);
        AnswerOption q1b = new AnswerOption("b", false);
        EntityIdSetter.setId(q1b, 12L);
        q1.addOption(q1a);
        q1.addOption(q1b);
        quiz.addQuestion(q1);

        when(quizRepository.findWithQuestionsById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(10L)).thenReturn(Optional.of(new User("S", "s@x", UserRole.STUDENT)));

        assertThrows(BadRequestException.class, () -> quizService.takeQuiz(10L, 100L, Map.of(1L, List.of(11L, 12L))));
    }

    @Test
    void takeQuiz_nonStudent_throwsConflict() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(new User("T", "t@x", UserRole.TEACHER)));
        assertThrows(ConflictException.class, () -> quizService.takeQuiz(10L, 100L, Map.of()));
    }
}
