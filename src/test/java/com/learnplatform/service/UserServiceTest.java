package com.learnplatform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.CourseReviewRepository;
import com.learnplatform.repository.EnrollmentRepository;
import com.learnplatform.repository.ProfileRepository;
import com.learnplatform.repository.QuizSubmissionRepository;
import com.learnplatform.repository.SubmissionRepository;
import com.learnplatform.repository.UserRepository;
import com.learnplatform.service.dto.UserCreateCommand;
import com.learnplatform.service.dto.UserUpdateCommand;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ProfileRepository profileRepository;
    @Mock
    CourseRepository courseRepository;
    @Mock
    EnrollmentRepository enrollmentRepository;
    @Mock
    SubmissionRepository submissionRepository;
    @Mock
    QuizSubmissionRepository quizSubmissionRepository;
    @Mock
    CourseReviewRepository courseReviewRepository;

    @InjectMocks
    UserService userService;

    @Test
    void create_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("x@x")).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> userService.create(new UserCreateCommand("n", "x@x", UserRole.STUDENT)));
    }

    @Test
    void update_emailTaken_throwsConflict() {
        User u = new User("n", "old@x", UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.existsByEmail("new@x")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.update(1L, new UserUpdateCommand(null, "new@x", null)));
    }

    @Test
    void delete_userReferenced_throwsConflict() {
        User u = new User("n", "x@x", UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(enrollmentRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.delete(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void delete_userNotReferenced_deletes() {
        User u = new User("n", "x@x", UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(courseRepository.existsByTeacherId(1L)).thenReturn(false);
        when(enrollmentRepository.existsByUserId(1L)).thenReturn(false);
        when(submissionRepository.existsByStudentId(1L)).thenReturn(false);
        when(quizSubmissionRepository.existsByStudentId(1L)).thenReturn(false);
        when(courseReviewRepository.existsByStudentId(1L)).thenReturn(false);

        userService.delete(1L);
        verify(userRepository).delete(u);
    }
}
