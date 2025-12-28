package com.learnplatform.service;

import com.learnplatform.entity.Enrollment;
import com.learnplatform.entity.EnrollmentStatus;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.EnrollmentRepository;
import com.learnplatform.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CourseRepository courseRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Enrollment enroll(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        if (user.getRole() != UserRole.STUDENT) {
            throw new ConflictException("Only STUDENT can enroll. userId=" + userId);
        }

        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found: id=" + courseId));

        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new ConflictException("User already enrolled: userId=" + userId + ", courseId=" + courseId);
        }

        Enrollment enrollment = new Enrollment(user, course, LocalDate.now(), EnrollmentStatus.ACTIVE);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void unenroll(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new NotFoundException("Enrollment not found: userId=" + userId + ", courseId=" + courseId));
        enrollmentRepository.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> listByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> listByUser(Long userId) {
        return enrollmentRepository.findByUserId(userId);
    }
}
