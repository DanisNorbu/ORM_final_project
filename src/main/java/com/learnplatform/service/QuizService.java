package com.learnplatform.service;

import com.learnplatform.entity.AnswerOption;
import com.learnplatform.entity.Module;
import com.learnplatform.entity.Question;
import com.learnplatform.entity.QuestionType;
import com.learnplatform.entity.Quiz;
import com.learnplatform.entity.QuizSubmission;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.BadRequestException;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.ModuleRepository;
import com.learnplatform.repository.QuizRepository;
import com.learnplatform.repository.QuizSubmissionRepository;
import com.learnplatform.repository.UserRepository;
import com.learnplatform.service.dto.QuizCreateCommand;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    public QuizService(
            QuizRepository quizRepository,
            QuizSubmissionRepository quizSubmissionRepository,
            UserRepository userRepository,
            ModuleRepository moduleRepository
    ) {
        this.quizRepository = quizRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
    }

    @Transactional
    public Quiz createQuiz(QuizCreateCommand cmd) {
        if (cmd.moduleId() == null) {
            throw new BadRequestException("moduleId is required");
        }
        Module module = moduleRepository.findById(cmd.moduleId())
                .orElseThrow(() -> new NotFoundException("Module not found: id=" + cmd.moduleId()));

        if (quizRepository.existsByModuleId(cmd.moduleId())) {
            throw new ConflictException("Module already has a quiz: moduleId=" + cmd.moduleId());
        }

        Quiz quiz = new Quiz(cmd.title(), cmd.timeLimit());
        module.setQuiz(quiz);

        if (cmd.questions() != null) {
            for (QuizCreateCommand.QuestionCreateCommand qcmd : cmd.questions()) {
                Question q = new Question(qcmd.text(), qcmd.type());
                quiz.addQuestion(q);
                if (qcmd.options() != null) {
                    for (QuizCreateCommand.AnswerOptionCreateCommand ocmd : qcmd.options()) {
                        AnswerOption opt = new AnswerOption(ocmd.text(), ocmd.isCorrect());
                        q.addOption(opt);
                    }
                }
            }
        }

        // Cascades from Module -> Quiz -> Question -> AnswerOption.
        moduleRepository.save(module);
        return quiz;
    }

    @Transactional(readOnly = true)
    public Quiz getQuizWithQuestionsOrThrow(Long quizId) {
        return quizRepository.findWithQuestionsById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found: id=" + quizId));
    }

    @Transactional
    public QuizSubmission takeQuiz(Long studentId, Long quizId, Map<Long, List<Long>> answers) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + studentId));
        if (student.getRole() != UserRole.STUDENT) {
            throw new ConflictException("Only STUDENT can take quiz. userId=" + studentId);
        }

        Quiz quiz = getQuizWithQuestionsOrThrow(quizId);

        int score = 0;
        for (Question question : quiz.getQuestions()) {
            Set<Long> correct = new HashSet<>();
            for (AnswerOption option : question.getOptions()) {
                if (option.isCorrect()) {
                    correct.add(option.getId());
                }
            }

            List<Long> selectedList = answers == null ? null : answers.get(question.getId());
            Set<Long> selected = new HashSet<>();
            if (selectedList != null) {
                selected.addAll(selectedList);
            }

            // Normalize SINGLE_CHOICE to at most 1 option.
            if (question.getType() == QuestionType.SINGLE_CHOICE && selected.size() > 1) {
                throw new BadRequestException("SINGLE_CHOICE question allows only one answer. questionId=" + question.getId());
            }

            if (selected.equals(correct)) {
                score++;
            }
        }

        QuizSubmission submission = new QuizSubmission(quiz, student, score, OffsetDateTime.now());
        return quizSubmissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmission> listResultsByQuiz(Long quizId) {
        return quizSubmissionRepository.findByQuizId(quizId);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmission> listResultsByStudent(Long studentId) {
        return quizSubmissionRepository.findByStudentId(studentId);
    }
}
