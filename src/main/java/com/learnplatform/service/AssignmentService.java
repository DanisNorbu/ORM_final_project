package com.learnplatform.service;

import com.learnplatform.entity.Assignment;
import com.learnplatform.entity.Lesson;
import com.learnplatform.entity.Submission;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.AssignmentRepository;
import com.learnplatform.repository.LessonRepository;
import com.learnplatform.repository.SubmissionRepository;
import com.learnplatform.repository.UserRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignmentService {

    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public AssignmentService(
            LessonRepository lessonRepository,
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            SubmissionRepository submissionRepository
    ) {
        this.lessonRepository = lessonRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional
    public Assignment createAssignment(Long lessonId, String title, String description, LocalDate dueDate, Integer maxScore) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found: id=" + lessonId));
        Assignment assignment = new Assignment(title, description, dueDate, maxScore);
        lesson.addAssignment(assignment);
        lessonRepository.save(lesson);
        return assignment;
    }

    @Transactional(readOnly = true)
    public Assignment getAssignmentOrThrow(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found: id=" + assignmentId));
    }

    @Transactional
    public Assignment updateAssignment(Long assignmentId, String title, String description, LocalDate dueDate, Integer maxScore) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        if (title != null) assignment.setTitle(title);
        if (description != null) assignment.setDescription(description);
        if (dueDate != null) assignment.setDueDate(dueDate);
        if (maxScore != null) assignment.setMaxScore(maxScore);
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteAssignment(Long assignmentId) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);

        if (submissionRepository.existsByAssignmentId(assignmentId)) {
            throw new ConflictException("Cannot delete assignment id=" + assignmentId + " because it has submissions");
        }

        assignmentRepository.delete(assignment);
    }

    @Transactional
    public Submission submit(Long assignmentId, Long studentId, String content) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + studentId));
        if (student.getRole() != UserRole.STUDENT) {
            throw new ConflictException("Only STUDENT can submit assignments. userId=" + studentId);
        }

        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId)) {
            throw new ConflictException("Submission already exists for assignmentId=" + assignmentId + ", studentId=" + studentId);
        }

        Submission submission = new Submission(assignment, student, OffsetDateTime.now(), content);
        assignment.addSubmission(submission);
        assignmentRepository.save(assignment);
        return submission;
    }

    @Transactional
    public Submission grade(Long submissionId, Integer score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found: id=" + submissionId));

        submission.setScore(score);
        submission.setFeedback(feedback);
        return submissionRepository.save(submission);
    }

    @Transactional
    public void deleteSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found: id=" + submissionId));
        submissionRepository.delete(submission);
    }

    @Transactional(readOnly = true)
    public List<Submission> listSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<Submission> listSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }
}
