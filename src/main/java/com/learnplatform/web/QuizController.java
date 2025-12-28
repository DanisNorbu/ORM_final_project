package com.learnplatform.web;

import com.learnplatform.entity.AnswerOption;
import com.learnplatform.entity.Question;
import com.learnplatform.entity.Quiz;
import com.learnplatform.entity.QuizSubmission;
import com.learnplatform.service.QuizService;
import com.learnplatform.service.dto.QuizCreateCommand;
import com.learnplatform.web.dto.QuizCreateRequest;
import com.learnplatform.web.dto.QuizDetailsView;
import com.learnplatform.web.dto.QuizSubmissionView;
import com.learnplatform.web.dto.TakeQuizRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Validated
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<QuizDetailsView> create(@Valid @RequestBody QuizCreateRequest req) {
        Quiz created = quizService.createQuiz(toCommand(req));
        Quiz full = quizService.getQuizWithQuestionsOrThrow(created.getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(toView(full));
    }

    @GetMapping("/{quizId}")
    public QuizDetailsView get(@PathVariable @Positive Long quizId) {
        return toView(quizService.getQuizWithQuestionsOrThrow(quizId));
    }

    @PostMapping("/{quizId}/take")
    public ResponseEntity<QuizSubmissionView> take(
            @PathVariable @Positive Long quizId,
            @Valid @RequestBody TakeQuizRequest req
    ) {
        QuizSubmission created = quizService.takeQuiz(req.studentId(), quizId, req.answers());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(toView(created));
    }

    @GetMapping("/{quizId}/results")
    public List<QuizSubmissionView> listResults(@PathVariable @Positive Long quizId) {
        return quizService.listResultsByQuiz(quizId).stream().map(QuizController::toView).toList();
    }

    @GetMapping("/students/{studentId}/results")
    public List<QuizSubmissionView> listStudentResults(@PathVariable @Positive Long studentId) {
        return quizService.listResultsByStudent(studentId).stream().map(QuizController::toView).toList();
    }

    private static QuizCreateCommand toCommand(QuizCreateRequest req) {
        List<QuizCreateCommand.QuestionCreateCommand> questions = null;
        if (req.questions() != null) {
            questions = req.questions().stream()
                    .map(q -> new QuizCreateCommand.QuestionCreateCommand(
                            q.text(),
                            q.type(),
                            q.options() == null ? null : q.options().stream()
                                    .map(o -> new QuizCreateCommand.AnswerOptionCreateCommand(o.text(), o.isCorrect()))
                                    .toList()
                    ))
                    .toList();
        }
        return new QuizCreateCommand(req.moduleId(), req.title(), req.timeLimit(), questions);
    }

    private static QuizDetailsView toView(Quiz quiz) {
        List<QuizDetailsView.QuestionView> qViews = quiz.getQuestions().stream().map(QuizController::toView).toList();
        return new QuizDetailsView(
                quiz.getId(),
                quiz.getModule().getId(),
                quiz.getTitle(),
                quiz.getTimeLimit(),
                qViews
        );
    }

    private static QuizDetailsView.QuestionView toView(Question q) {
        List<QuizDetailsView.AnswerOptionView> oViews = q.getOptions().stream().map(QuizController::toView).toList();
        return new QuizDetailsView.QuestionView(q.getId(), q.getText(), q.getType(), oViews);
    }

    private static QuizDetailsView.AnswerOptionView toView(AnswerOption o) {
        return new QuizDetailsView.AnswerOptionView(o.getId(), o.getText(), o.isCorrect());
    }

    private static QuizSubmissionView toView(QuizSubmission s) {
        return new QuizSubmissionView(
                s.getId(),
                s.getQuiz().getId(),
                s.getStudent().getId(),
                s.getScore(),
                s.getTakenAt()
        );
    }
}
