package com.learnplatform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    LessonRepository lessonRepository;
    @Mock
    AssignmentRepository assignmentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    SubmissionRepository submissionRepository;

    @InjectMocks
    AssignmentService assignmentService;

    @Test
    void createAssignment_attachesToLesson() {
        Lesson lesson = new Lesson("L1", "c", null);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        Assignment a = assignmentService.createAssignment(1L, "A", "d", LocalDate.now(), 100);

        assertNotNull(a);
        assertEquals(lesson, a.getLesson());
        assertEquals(1, lesson.getAssignments().size());
    }

    @Test
    void createAssignment_missingLesson_throwsNotFound() {
        when(lessonRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> assignmentService.createAssignment(1L, "A", null, null, null));
    }

    @Test
    void submit_duplicate_throwsConflict() {
        Assignment assignment = new Assignment("A", null, null, 100);
        User student = new User("S", "s@x", UserRole.STUDENT);

        when(assignmentRepository.findById(5L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(submissionRepository.existsByAssignmentIdAndStudentId(5L, 10L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> assignmentService.submit(5L, 10L, "work"));
    }

    @Test
    void submit_nonStudent_throwsConflict() {
        Assignment assignment = new Assignment("A", null, null, 100);
        User teacher = new User("T", "t@x", UserRole.TEACHER);

        when(assignmentRepository.findById(5L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(10L)).thenReturn(Optional.of(teacher));

        assertThrows(ConflictException.class, () -> assignmentService.submit(5L, 10L, "work"));
    }

    @Test
    void grade_updatesFields() {
        Submission s = new Submission(new Assignment("A", null, null, 100), new User("S", "s@x", UserRole.STUDENT),
                java.time.OffsetDateTime.now(), "c");
        when(submissionRepository.findById(7L)).thenReturn(Optional.of(s));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        Submission updated = assignmentService.grade(7L, 90, "ok");

        assertEquals(90, updated.getScore());
        assertEquals("ok", updated.getFeedback());
    }
}
