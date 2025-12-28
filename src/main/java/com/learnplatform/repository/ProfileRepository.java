package com.learnplatform.repository;

import com.learnplatform.entity.Profile;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByUserId(Long userId);

    Optional<Profile> findByUserId(Long userId);
}
