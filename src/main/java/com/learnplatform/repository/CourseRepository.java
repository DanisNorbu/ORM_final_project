package com.learnplatform.repository;

import com.learnplatform.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    java.util.List<Course> findByCategoryId(Long categoryId);

    boolean existsByTeacherId(Long teacherId);

    boolean existsByCategoryId(Long categoryId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"modules", "modules.lessons", "tags"})
    java.util.Optional<Course> findWithStructureById(Long id);

}
