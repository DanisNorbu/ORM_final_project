package com.learnplatform.web;

import com.learnplatform.entity.Profile;
import com.learnplatform.service.ProfileService;
import com.learnplatform.web.dto.ProfileUpsertRequest;
import com.learnplatform.web.dto.ProfileView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/profile")
@Validated
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileView get(@PathVariable @Positive Long userId) {
        Profile profile = profileService.getByUserIdOrThrow(userId);
        return toView(profile);
    }

    @PutMapping
    public ProfileView upsert(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody ProfileUpsertRequest req
    ) {
        Profile saved = profileService.upsert(userId, req.bio(), req.avatarUrl());
        return toView(saved);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable @Positive Long userId) {
        profileService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    private static ProfileView toView(Profile p) {
        return new ProfileView(
                p.getId(),
                p.getUser().getId(),
                p.getBio(),
                p.getAvatarUrl()
        );
    }
}
