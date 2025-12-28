package com.learnplatform.repository;

import com.learnplatform.entity.Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    @EntityGraph(attributePaths = {"course", "lessons"})
    Optional<Module> findWithLessonsById(Long id);
}
