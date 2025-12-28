package com.learnplatform.service;

import com.learnplatform.entity.Profile;
import com.learnplatform.entity.User;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.ProfileRepository;
import com.learnplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public ProfileService(UserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public Profile getByUserIdOrThrow(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found for userId=" + userId));

        // Make user id available for serialization while open-in-view is disabled.
        profile.getUser().getId();
        return profile;
    }

    @Transactional
    public Profile upsert(Long userId, String bio, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile(bio, avatarUrl);
            user.setProfile(profile);
            userRepository.save(user);
            profile.getUser().getId();
            return profile;
        }

        if (bio != null) profile.setBio(bio);
        if (avatarUrl != null) profile.setAvatarUrl(avatarUrl);

        Profile saved = profileRepository.save(profile);
        saved.getUser().getId();
        return saved;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));

        if (user.getProfile() == null) {
            throw new NotFoundException("Profile not found for userId=" + userId);
        }

        // orphanRemoval on User.profile will remove the Profile row.
        user.setProfile(null);
        userRepository.save(user);
    }
}
