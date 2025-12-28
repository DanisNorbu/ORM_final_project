package com.learnplatform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.learnplatform.entity.Category;
import com.learnplatform.entity.Course;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.repository.CategoryRepository;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.CourseReviewRepository;
import com.learnplatform.repository.EnrollmentRepository;
import com.learnplatform.repository.LessonRepository;
import com.learnplatform.repository.ModuleRepository;
import com.learnplatform.repository.QuizSubmissionRepository;
import com.learnplatform.repository.SubmissionRepository;
import com.learnplatform.repository.TagRepository;
import com.learnplatform.repository.UserRepository;
import com.learnplatform.service.dto.CourseCreateCommand;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    CourseRepository courseRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    TagRepository tagRepository;
    @Mock
    ModuleRepository moduleRepository;
    @Mock
    LessonRepository lessonRepository;
    @Mock
    EnrollmentRepository enrollmentRepository;
    @Mock
    SubmissionRepository submissionRepository;
    @Mock
    QuizSubmissionRepository quizSubmissionRepository;
    @Mock
    CourseReviewRepository courseReviewRepository;

    @InjectMocks
    CourseService courseService;

    @Test
    void createCourse_teacherMustBeTeacher() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category("C")));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User("u", "u@x", UserRole.STUDENT)));

        assertThrows(ConflictException.class, () -> courseService.create(new CourseCreateCommand(
                "t",
                "d",
                1L,
                2L,
                "1w",
                LocalDate.now(),
                Set.of("java")
        )));
    }

    @Test
    void deleteCourse_withLearningData_throwsConflict() {
        Course course = mock(Course.class);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseId(10L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> courseService.delete(10L));
        verify(courseRepository, never()).delete(any());
    }
}
