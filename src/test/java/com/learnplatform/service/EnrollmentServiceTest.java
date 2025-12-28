package com.learnplatform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.learnplatform.entity.Course;
import com.learnplatform.entity.Enrollment;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.EnrollmentRepository;
import com.learnplatform.repository.UserRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    EnrollmentRepository enrollmentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    EnrollmentService enrollmentService;

    @Test
    void enroll_createsEnrollment() {
        User student = new User("S", "s@x", UserRole.STUDENT);
        Course course = mock(Course.class);

        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(20L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(10L, 20L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        Enrollment e = enrollmentService.enroll(10L, 20L);

        assertNotNull(e);
        assertEquals(student, e.getUser());
        assertEquals(course, e.getCourse());
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enroll_duplicate_throwsConflict() {
        User student = new User("S", "s@x", UserRole.STUDENT);
        Course course = mock(Course.class);

        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(20L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(10L, 20L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> enrollmentService.enroll(10L, 20L));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_nonStudent_throwsConflict() {
        User teacher = new User("T", "t@x", UserRole.TEACHER);
        when(userRepository.findById(10L)).thenReturn(Optional.of(teacher));

        assertThrows(ConflictException.class, () -> enrollmentService.enroll(10L, 20L));
    }

    @Test
    void unenroll_deletesEnrollment() {
        Enrollment e = mock(Enrollment.class);
        when(enrollmentRepository.findByUserIdAndCourseId(10L, 20L)).thenReturn(Optional.of(e));

        enrollmentService.unenroll(10L, 20L);

        verify(enrollmentRepository).delete(e);
    }

    @Test
    void unenroll_missing_throwsNotFound() {
        when(enrollmentRepository.findByUserIdAndCourseId(10L, 20L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> enrollmentService.unenroll(10L, 20L));
    }
}
