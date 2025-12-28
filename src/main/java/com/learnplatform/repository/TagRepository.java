package com.learnplatform.repository;

import com.learnplatform.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    java.util.Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @org.springframework.data.jpa.repository.Query(
            "select (count(c) > 0) from Tag t join t.courses c where t.id = :tagId"
    )
    boolean isUsedByAnyCourse(@org.springframework.data.repository.query.Param("tagId") Long tagId);


}
