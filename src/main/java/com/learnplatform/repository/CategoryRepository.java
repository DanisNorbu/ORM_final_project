package com.learnplatform.repository;

import com.learnplatform.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    java.util.Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);


}
