package com.learnplatform.service;

import com.learnplatform.entity.Course;
import com.learnplatform.entity.CourseReview;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.CourseReviewRepository;
import com.learnplatform.repository.UserRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseReviewService(
            CourseReviewRepository courseReviewRepository,
            CourseRepository courseRepository,
            UserRepository userRepository
    ) {
        this.courseReviewRepository = courseReviewRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CourseReview create(Long courseId, Long studentId, int rating, String comment) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found: id=" + courseId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found: id=" + studentId));
        if (student.getRole() != UserRole.STUDENT) {
            throw new ConflictException("User id=" + studentId + " is not a STUDENT");
        }
        if (courseReviewRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new ConflictException("Review already exists for courseId=" + courseId + " and studentId=" + studentId);
        }

        CourseReview review = new CourseReview(course, student, rating, comment, OffsetDateTime.now(ZoneOffset.UTC));
        return courseReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public CourseReview getOrThrow(Long id) {
        return courseReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<CourseReview> listByCourse(Long courseId) {
        return courseReviewRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseReview> listByStudent(Long studentId) {
        return courseReviewRepository.findByStudentId(studentId);
    }

    @Transactional
    public CourseReview update(Long reviewId, Integer rating, String comment) {
        CourseReview review = getOrThrow(reviewId);
        if (rating != null) review.setRating(rating);
        if (comment != null) review.setComment(comment);
        return courseReviewRepository.save(review);
    }

    @Transactional
    public void delete(Long reviewId) {
        CourseReview review = getOrThrow(reviewId);
        courseReviewRepository.delete(review);
    }
}
