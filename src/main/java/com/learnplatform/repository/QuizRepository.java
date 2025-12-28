package com.learnplatform.repository;

import com.learnplatform.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    boolean existsByModuleId(Long moduleId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"questions", "questions.options"})
    java.util.Optional<Quiz> findWithQuestionsById(Long id);

}
