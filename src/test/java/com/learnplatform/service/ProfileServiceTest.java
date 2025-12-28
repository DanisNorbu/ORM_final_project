package com.learnplatform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.learnplatform.entity.Profile;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.ProfileRepository;
import com.learnplatform.repository.UserRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ProfileRepository profileRepository;

    @InjectMocks
    ProfileService profileService;

    @Test
    void getByUserId_profileMissing_throwsNotFound() {
        when(profileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> profileService.getByUserIdOrThrow(10L));
    }

    @Test
    void upsert_createsNewProfile_whenUserHasNone() {
        User user = new User("Alice", "alice@x", UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Profile created = profileService.upsert(1L, "bio", "url");

        assertNotNull(created);
        assertEquals("bio", created.getBio());
        verify(userRepository).save(user);
    }

    @Test
    void deleteByUserId_noProfile_throwsNotFound() {
        User user = new User("Bob", "bob@x", UserRole.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class, () -> profileService.deleteByUserId(2L));
    }
}
