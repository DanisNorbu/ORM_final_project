package com.learnplatform.repository;

import com.learnplatform.entity.Submission;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    boolean existsByStudentId(Long studentId);

    boolean existsByAssignmentId(Long assignmentId);

    @Query(
            "select (count(s) > 0) from Submission s "
                    + "where s.assignment.lesson.id = :lessonId"
    )
    boolean existsByLessonId(@Param("lessonId") Long lessonId);

    @Query(
            "select (count(s) > 0) from Submission s "
                    + "where s.assignment.lesson.module.id = :moduleId"
    )
    boolean existsByModuleId(@Param("moduleId") Long moduleId);

    @Query(
            "select (count(s) > 0) from Submission s "
                    + "where s.assignment.lesson.module.course.id = :courseId"
    )
    boolean existsByCourseId(@Param("courseId") Long courseId);

    List<Submission> findByAssignmentId(Long assignmentId);

    List<Submission> findByStudentId(Long studentId);
}
