package com.learnplatform.repository;

import com.learnplatform.entity.QuizSubmission;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    boolean existsByStudentId(Long studentId);

    @Query(
            "select (count(qs) > 0) from QuizSubmission qs "
                    + "where qs.quiz.module.course.id = :courseId"
    )
    boolean existsByCourseId(@Param("courseId") Long courseId);

    @Query(
            "select (count(qs) > 0) from QuizSubmission qs "
                    + "where qs.quiz.module.id = :moduleId"
    )
    boolean existsByModuleId(@Param("moduleId") Long moduleId);

    List<QuizSubmission> findByStudentId(Long studentId);

    List<QuizSubmission> findByQuizId(Long quizId);
}
