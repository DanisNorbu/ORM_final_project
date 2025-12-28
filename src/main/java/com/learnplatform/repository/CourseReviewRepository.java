package com.learnplatform.repository;

import com.learnplatform.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    boolean existsByCourseId(Long courseId);

    boolean existsByStudentId(Long studentId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    java.util.List<CourseReview> findByCourseId(Long courseId);

    java.util.List<CourseReview> findByStudentId(Long studentId);

}
