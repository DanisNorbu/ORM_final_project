package com.learnplatform.repository;

import com.learnplatform.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserId(Long userId);

    boolean existsByCourseId(Long courseId);

    java.util.Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    java.util.List<Enrollment> findByCourseId(Long courseId);

    java.util.List<Enrollment> findByUserId(Long userId);

}
